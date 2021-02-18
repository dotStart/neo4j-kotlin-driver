package tv.dotstart.neo4j.kotlin.testkit.codec

import com.fasterxml.jackson.databind.ObjectMapper
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.MessageToMessageEncoder
import org.apache.logging.log4j.LogManager
import tv.dotstart.neo4j.kotlin.testkit.message.response.CommandResponse

class CommandResponseEncoder(mapper: ObjectMapper) : MessageToMessageEncoder<CommandResponse>() {

    private val logger = LogManager.getLogger("command")

    private val writer = mapper.writerFor(CommandResponse::class.java)

    override fun encode(ctx: ChannelHandlerContext, msg: CommandResponse, out: MutableList<Any>) {
        logger.debug("<< $msg")

        out.add(this.writer.writeValueAsString(msg))
    }
}