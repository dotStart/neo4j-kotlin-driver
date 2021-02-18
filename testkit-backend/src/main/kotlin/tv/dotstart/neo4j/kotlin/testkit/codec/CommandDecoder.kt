package tv.dotstart.neo4j.kotlin.testkit.codec

import com.fasterxml.jackson.databind.ObjectMapper
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.MessageToMessageDecoder
import org.apache.logging.log4j.LogManager
import tv.dotstart.neo4j.kotlin.testkit.message.command.Command

class CommandDecoder(
    mapper: ObjectMapper
) : MessageToMessageDecoder<String>() {

    private val logger = LogManager.getLogger("command")

    private val reader = mapper.readerFor(Command::class.java)

    override fun decode(ctx: ChannelHandlerContext, msg: String, out: MutableList<Any>) {
        val command = this.reader.readValue<Command>(msg)

        logger.debug(">> $command")
        out += command
    }
}