package tv.dotstart.neo4j.kotlin.testkit.codec

import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.MessageToMessageDecoder
import org.apache.logging.log4j.LogManager

/**
 * Handles the separation of frames within the testkit protocol.
 *
 * Each respective request is enclosed by a block beginning with the magic string `#request begin` and ends with
 * `#request end`. Any data in between the enclosing markers is treated as part of the request.
 */
class RequestFrameDecoder : MessageToMessageDecoder<String>() {
    private var requestStarted = false
    private val buffer = StringBuilder()

    private val logger = LogManager.getLogger("wire")

    override fun decode(ctx: ChannelHandlerContext, msg: String, out: MutableList<Any>) {
        when (msg.trimEnd()) {
            "#request begin" -> {
                require(!this.requestStarted) { "Request already active" }
                this.requestStarted = true
            }
            "#request end" -> {
                require(this.requestStarted) { "No active request" }
                this.requestStarted = false

                val frame = this.buffer.toString()
                logger.debug(">> $frame")

                out.add(frame)
                this.buffer.clear()

            }
            else -> {
                require(this.requestStarted) { "No active request" }
                this.buffer.append(msg)
            }
        }
    }
}