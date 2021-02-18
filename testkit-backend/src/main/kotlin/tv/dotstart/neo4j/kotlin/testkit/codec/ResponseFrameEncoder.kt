package tv.dotstart.neo4j.kotlin.testkit.codec

import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.MessageToMessageEncoder
import org.apache.logging.log4j.LogManager

/**
 * Handles the separation of frames within the testkit protocol.
 *
 * Each respective response is enclosed by a block beginning with the magic string `#response begin` and ends with
 * `#response end`. Any data in between the enclosing markers is treated as part of the response.
 */
class ResponseFrameEncoder : MessageToMessageEncoder<String>() {

    private val logger = LogManager.getLogger("wire")

    override fun encode(ctx: ChannelHandlerContext, msg: String, out: MutableList<Any>) {
        logger.debug("<< $msg")

        out += "#response begin\n"
        out += "$msg\n"
        out += "#response end\n"
    }
}