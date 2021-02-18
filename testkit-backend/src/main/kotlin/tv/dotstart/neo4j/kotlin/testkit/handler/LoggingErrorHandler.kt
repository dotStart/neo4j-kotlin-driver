package tv.dotstart.neo4j.kotlin.testkit.handler

import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandlerAdapter
import org.apache.logging.log4j.LogManager

class LoggingErrorHandler : ChannelInboundHandlerAdapter() {

    private val logger = LogManager.getLogger("channel")

    override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
        super.exceptionCaught(ctx, cause)

        logger.error("Uncaught exception within connection with ${ctx.channel().remoteAddress()}", cause)
    }

    override fun channelInactive(ctx: ChannelHandlerContext) {
        super.channelInactive(ctx)

        logger.info("Connection with ${ctx.channel().remoteAddress()} has been closed")
    }
}