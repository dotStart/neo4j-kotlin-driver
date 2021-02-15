package com.neo4j.driver.kotlin.network.handler.util

import com.neo4j.driver.kotlin.error.DriverConnectionException
import com.neo4j.driver.kotlin.util.getUnwrapped
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandlerAdapter
import java.util.concurrent.CompletableFuture
import kotlin.reflect.KClass

/**
 * Provides await support for network operations when dealing with synchronous operations via a netty channel.
 *
 * This implementation is primarily provided as a simple method for suspending application logic until a given expected
 * message is received and should typically be paired with a timeout in order to prevent indefinite wait times.
 */
class SuspendingMessageHandler<M : Any>(private val type: KClass<M>) : ChannelInboundHandlerAdapter() {
    private val future = CompletableFuture<M>()

    /**
     * Awaits a message of a given target type via the connection.
     *
     * This method will either return an instance of the desired message or, if an error occurs or the connection is
     * terminated in the meantime, an exception.
     *
     * @throws DriverConnectionException when the connection is terminated or an error is raised.
     */
    fun await(): M {
        return this.future.getUnwrapped()
    }

    override fun channelRead(ctx: ChannelHandlerContext, msg: Any) {
        if (!this.type.isInstance(msg)) {
            return super.channelRead(ctx, msg)
        }

        @Suppress("UNCHECKED_CAST")
        this.future.complete(msg as M)
        ctx.pipeline().remove(this)
    }

    override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
        this.future.completeExceptionally(
            DriverConnectionException(
                "Exception caught while awaiting message",
                cause
            )
        )
        ctx.pipeline().remove(this)

        @Suppress("DEPRECATION")
        super.exceptionCaught(ctx, cause)
    }

    override fun channelInactive(ctx: ChannelHandlerContext) {
        this.future.completeExceptionally(DriverConnectionException("Connection closed"))
        ctx.pipeline().remove(this)

        super.channelInactive(ctx)
    }
}