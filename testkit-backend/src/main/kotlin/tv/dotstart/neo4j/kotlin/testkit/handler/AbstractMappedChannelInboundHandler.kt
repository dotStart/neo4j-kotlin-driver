package tv.dotstart.neo4j.kotlin.testkit.handler

import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandlerAdapter
import tv.dotstart.neo4j.kotlin.testkit.handler.annotation.CommandHandler
import tv.dotstart.neo4j.kotlin.testkit.message.command.Command
import java.lang.reflect.InvocationTargetException
import kotlin.reflect.full.functions
import kotlin.reflect.full.hasAnnotation
import kotlin.reflect.jvm.jvmErasure

abstract class AbstractMappedChannelInboundHandler : ChannelInboundHandlerAdapter() {

    private val functionMap = this::class.functions
        .filter { it.hasAnnotation<CommandHandler>() }
        .map {
            if (it.parameters.size != 3) {
                throw IllegalStateException("Illegal handler configuration: Expected command handler to accept context and message type respectively")
            }

            it.parameters[2].type.jvmErasure to it
        }
        .toMap()

    override fun channelRead(ctx: ChannelHandlerContext, msg: Any) {
        if (msg !is Command) {
            return super.channelRead(ctx, msg)
        }

        val command = msg.parameters

        val messageType = command::class
        val handlerFunc = this.functionMap[messageType]
            ?: return super.channelRead(ctx, command)

        try {
            handlerFunc.call(this, ctx, command)
        } catch (ex: InvocationTargetException) {
            val cause = ex.cause
            if (cause != null) {
                throw cause
            }

            throw ex
        }
    }
}