package tv.dotstart.neo4j.kotlin.testkit.handler

import com.fasterxml.jackson.databind.ObjectMapper
import io.netty.channel.Channel
import io.netty.channel.ChannelInitializer
import io.netty.handler.codec.DelimiterBasedFrameDecoder
import io.netty.handler.codec.Delimiters
import io.netty.handler.codec.string.StringDecoder
import io.netty.handler.codec.string.StringEncoder
import org.apache.logging.log4j.LogManager
import tv.dotstart.neo4j.kotlin.testkit.codec.CommandDecoder
import tv.dotstart.neo4j.kotlin.testkit.codec.CommandResponseEncoder
import tv.dotstart.neo4j.kotlin.testkit.codec.RequestFrameDecoder
import tv.dotstart.neo4j.kotlin.testkit.codec.ResponseFrameEncoder

class TestkitServerChannelInitializer(private val mapper: ObjectMapper) : ChannelInitializer<Channel>() {

    override fun initChannel(ch: Channel) {
        LogManager.getLogger("channel")
            .info("Incoming connection from ${ch.remoteAddress()}")

        try {
            ch.pipeline()
                .addLast("delimiterFrameDecoder", DelimiterBasedFrameDecoder(8192, *Delimiters.lineDelimiter()))
                .addLast("stringDecoder", StringDecoder(Charsets.UTF_8))
                .addLast("stringEncoder", StringEncoder(Charsets.UTF_8))
                .addLast("requestFrameDecoder", RequestFrameDecoder())
                .addLast("responseFrameEncoder", ResponseFrameEncoder())
                .addLast("commandDecoder", CommandDecoder(this.mapper))
                .addLast("commandResponseEncoder", CommandResponseEncoder(this.mapper))
                .addLast("testkitHandler", TestkitHandler())
                .addLast("errorHandler", LoggingErrorHandler())
        } catch (ex: Throwable) {
            ex.printStackTrace()
        }
    }
}