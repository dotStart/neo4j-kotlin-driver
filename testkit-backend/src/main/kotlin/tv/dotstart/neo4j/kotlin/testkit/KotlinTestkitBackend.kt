package tv.dotstart.neo4j.kotlin.testkit

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.nio.NioServerSocketChannel
import org.apache.logging.log4j.LogManager
import tv.dotstart.neo4j.kotlin.testkit.handler.TestkitServerChannelInitializer

fun main(args: Array<String>) {
    val logger = LogManager.getLogger("testkit-kotlin-backend")

    logger.info("Initializing server thread pools")
    val bossGroup = NioEventLoopGroup()
    val workerGroup = NioEventLoopGroup()

    val mapper = jacksonObjectMapper()

    logger.info("Initializing primary testkit listener")
    val future = ServerBootstrap()
        .group(bossGroup, workerGroup)
        .channel(NioServerSocketChannel::class.java)
        .childHandler(TestkitServerChannelInitializer(mapper))
        .bind(9876)
        .sync()

    if (!future.isSuccess) {
        val cause = future.cause()

        throw IllegalStateException("testkit backend startup failed", cause)
    }

    val channel = future.channel()
    logger.info("testkit backend is listening on ${channel.localAddress()}")

    future.channel().closeFuture().await()
}