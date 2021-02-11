package com.neo4j.driver.kotlin.network.protocol

import com.neo4j.driver.kotlin.delegate.log4j
import com.neo4j.driver.kotlin.error.DriverAuthenticationException
import com.neo4j.driver.kotlin.network.codec.ChunkedFrameDecoder
import com.neo4j.driver.kotlin.network.codec.ChunkedFrameEncoder
import com.neo4j.driver.kotlin.network.codec.PackstreamMessageDecoder
import com.neo4j.driver.kotlin.network.codec.PackstreamMessageEncoder
import com.neo4j.driver.kotlin.network.protocol.bolt42.error.SecurityErrors
import com.neo4j.driver.kotlin.network.protocol.bolt42.message.request.connected.HelloMessage
import com.neo4j.driver.kotlin.network.protocol.bolt42.message.request.ready.GoodbyeMessage
import com.neo4j.driver.kotlin.network.protocol.bolt42.message.request.streaming.PullMessage
import com.neo4j.driver.kotlin.network.protocol.bolt42.message.request.streaming.RecordMessage
import com.neo4j.driver.kotlin.network.protocol.bolt42.message.response.FailureMessage
import com.neo4j.driver.kotlin.network.protocol.bolt42.message.response.IgnoredMessage
import com.neo4j.driver.kotlin.network.protocol.bolt42.message.response.RunMessage
import com.neo4j.driver.kotlin.network.protocol.bolt42.message.response.SuccessMessage
import com.neo4j.driver.kotlin.network.protocol.bolt42.handler.RequestManager
import com.neo4j.driver.kotlin.network.protocol.bolt42.handler.RecordCollector
import com.neo4j.driver.kotlin.network.protocol.error.ProtocolOperationException
import com.neo4j.driver.kotlin.network.protocol.error.ProtocolViolationException
import com.neo4j.driver.kotlin.packstream.mapper.PackstreamMapper
import com.neo4j.driver.kotlin.packstream.mapper.registry.DefaultStructureRegistry
import com.neo4j.driver.kotlin.util.ProtocolVersion
import com.neo4j.driver.kotlin.util.getUnwrapped
import io.netty.channel.Channel

/**
 * Provides a bolt protocol implementation for the 4.2 protocol revision.
 */
class BoltProtocol42(
    ch: Channel,
    private val requestManager: RequestManager
) : BoltProtocol(ch) {

    override val version
        get() = Companion.version

    override fun handshake(scheme: String, principal: String?, credentials: String?) {
        val result = try {
            this.requestManager.sendRequest(
                HelloMessage(
                    HelloMessage.Parameters(
                        "neo4j-kotlin/0.1.0", // TODO: version metadata
                        scheme, principal, credentials
                    )
                )
            ).getUnwrapped() // TODO: Timeout
        } catch (ex: ProtocolOperationException) {
            throw when (ex.code) {
                in SecurityErrors.authenticationRateLimit -> DriverAuthenticationException(
                    "Maximum authentication attempts exceeded",
                    ex
                )
                in SecurityErrors.credentialsExpired -> DriverAuthenticationException("Credentials expired", ex)
                in SecurityErrors.unauthorized -> DriverAuthenticationException("Illegal credentials", ex)
                else -> ex // something else happened - rethrow
            }
        }

        if (!result.value) {
            throw ProtocolViolationException("Failed to perform handshake: Server ignored request")
        }

        logger.debug("Connection entered READY state - ${result.metadata}")
    }

    @OptIn(ExperimentalStdlibApi::class)
    override fun run(query: String, parameters: Map<String, Any?>): List<Map<String, Any?>> {
        logger.debug("Running query: $query") // TODO: too chatty

        val runResult =
            this.requestManager.sendRequest(
                RunMessage(
                    query,
                    parameters
                )
            ).getUnwrapped()

        if (!runResult.value) {
            // TODO: Retry
            throw ProtocolViolationException("Query was ignored")
        }

        logger.debug("Query execution metadata: ${runResult.metadata}")

        @Suppress("UNCHECKED_CAST") // TODO: Validation?
        val fieldNames = runResult.metadata["fields"] as? List<String>
            ?: emptyList()

        val collector = RecordCollector()
        this.channel.pipeline()
            .addLast(collector)

        try {
            val pullResult = this.requestManager.sendRequest(
                PullMessage() // TODO: Streaming
            ).getUnwrapped()

            if (!pullResult.value) {
                // TODO: Retry
                throw ProtocolViolationException("Query was ignored")
            }

            logger.debug("Pull metadata: ${pullResult.metadata}")

            return buildList {
                collector.content
                    .forEach { recordValues ->
                        val recordMap = buildMap<String, Any?> {
                            recordValues.forEachIndexed { i, content ->
                                val fieldName = if (i < fieldNames.size) {
                                    fieldNames[i]
                                } else {
                                    "__unknown_field_$i"
                                }

                                put(fieldName, content)
                            }
                        }

                        add(recordMap)
                    }
            }
        } finally {
            this.channel.pipeline()
                .remove(collector)
        }
    }

    override fun close() {
        logger.debug("Sending goodbye message")
        this.channel.writeAndFlush(GoodbyeMessage()) // TODO: Requires handshake to be complete

        logger.info("Closing connection to ${channel.remoteAddress()} (with local address ${channel.localAddress()})")
        super.close()
    }

    companion object {
        private val logger by log4j()

        /**
         * Identifies the protocol version which has been chosen for this particular implementation of the protocol.
         */
        val version = ProtocolVersion(4, 2)

        private val structureRegistry = DefaultStructureRegistry.create {
            register(HelloMessage::class)
            register(GoodbyeMessage::class)

            register(SuccessMessage::class)
            register(FailureMessage::class)
            register(IgnoredMessage::class)

            register(PullMessage::class)
            register(RecordMessage::class)
        }
    }

    /**
     * Constructs new bolt 4.2 connections for a given connection channel.
     */
    class Factory : BoltProtocol.Factory {

        override val version
            get() = Companion.version

        override fun create(ch: Channel): BoltProtocol42 {
            val requestManager = RequestManager()

            ch.pipeline()
                .addLast("chunkedFrameEncoder", ChunkedFrameEncoder())
                .addLast("chunkedFrameDecoder", ChunkedFrameDecoder())
                .addLast("packstreamMessageEncoder", PackstreamMessageEncoder(PackstreamMapper.default))
                .addLast(
                    "packstreamMessageDecoder",
                    PackstreamMessageDecoder(PackstreamMapper.default, structureRegistry)
                )
                .addLast(requestManager)

            return BoltProtocol42(ch, requestManager)
        }
    }
}