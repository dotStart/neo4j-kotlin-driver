package com.neo4j.driver.kotlin

import com.neo4j.driver.kotlin.delegate.log4j
import com.neo4j.driver.kotlin.error.DriverConnectionException
import com.neo4j.driver.kotlin.network.await
import com.neo4j.driver.kotlin.network.codec.ClientHandshakeEncoder
import com.neo4j.driver.kotlin.network.codec.ServerHandshakeDecoder
import com.neo4j.driver.kotlin.network.handler.handshake.ClientNegotationInitializationHandler
import com.neo4j.driver.kotlin.network.packet.ClientHandshakePacket
import com.neo4j.driver.kotlin.network.packet.ServerHandshakePacket
import com.neo4j.driver.kotlin.network.protocol.BoltProtocol
import com.neo4j.driver.kotlin.network.transport.NetworkTransport
import com.neo4j.driver.kotlin.util.NumberedFormatThreadFactory
import com.neo4j.driver.kotlin.util.ProtocolVersion
import io.netty.bootstrap.Bootstrap
import java.io.Closeable
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.URI

class Driver(
    val uri: URI,
    private val username: String? = null,
    private val password: String? = null
) : Closeable, AutoCloseable {

    /**
     * Caches the optimal network transport for the current execution environment.
     */
    private val transport = NetworkTransport.optimal

    /**
     * Provides a worker pool for use with networking operations throughout this driver instance.
     */
    private val workerPool = this.transport.createEventLoopGroup(
        threadFactory = NumberedFormatThreadFactory("n4j-driver-%d")
    )

    companion object {
        private val logger by log4j()
    }

    init {
        require(uri.scheme == "bolt" || uri.scheme == "neo4j") { "Expected bolt:// or neo4j:// scheme" }
        require((this.username == null && this.password == null) || (this.username != null && this.password != null)) {
            "Invalid credential pair: Must specify both username and password or omit both"
        }

        this.transport.apply {
            logger.info("Chosen $name as network transport")
        }
    }

    fun connect(): BoltProtocol {
        val host = InetAddress.getByName(this.uri.host)
        val port = this.uri.port
            .takeIf { it != -1 }
            ?: 7687

        val address = InetSocketAddress(host, port)

        val bootstrap = Bootstrap()
            .channel(this.transport.socketChannelType.java)
            .group(this.workerPool)
            .handler(ClientNegotationInitializationHandler())

        logger.info("Establishing connection with $address")
        val future = bootstrap.connect(address)
            .await()

        if (!future.isSuccess) {
            throw DriverConnectionException("Failed to establish connection to $address", future.cause())
        }

        val channel = future.channel()

        val versionMap = BoltProtocol.installed
            .map { it.version to it }
            .toMap()

        logger.debug("Transmitting handshake with supported versions: " + versionMap.keys.joinToString(", "))
        val handshake = channel.await<ServerHandshakePacket> {
            it.writeAndFlush(ClientHandshakePacket(supportedVersions = versionMap.keys.toList()))
        }

        val negotiatedProtocol = handshake.negotiatedVersion
            .takeIf { it != ProtocolVersion.unsupported }
            ?.let { versionMap[it] }
            ?: throw DriverConnectionException("Failed to establish connection to $address: No supported protocol dialect negotiated")

        handshake.apply { logger.info("Negotiated protocol version $negotiatedVersion") }

        logger.debug("Removing handshake handlers prior to protocol initialization")
        channel.pipeline()
            .apply {
                remove(ServerHandshakeDecoder::class.java)
                remove(ClientHandshakeEncoder::class.java)
            }

        logger.debug("Wrapping channel in preparation for protocol switch")
        val connection = negotiatedProtocol.create(channel)

        logger.debug("Performing protocol specific handshake")
        val authenticationScheme = if (this.username != null && this.password != null) {
            "basic"
        } else {
            "none"
        }
        connection.handshake(authenticationScheme, this.username, this.password)

        return connection
    }

    override fun close() {
        this.workerPool.shutdownGracefully()
            .await()
    }
}