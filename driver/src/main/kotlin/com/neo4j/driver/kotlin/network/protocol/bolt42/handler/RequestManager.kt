package com.neo4j.driver.kotlin.network.protocol.bolt42.handler

import com.neo4j.driver.kotlin.error.DriverConnectionException
import com.neo4j.driver.kotlin.network.protocol.bolt42.message.response.FailureMessage
import com.neo4j.driver.kotlin.network.protocol.bolt42.message.response.IgnoredMessage
import com.neo4j.driver.kotlin.network.protocol.bolt42.message.response.SuccessMessage
import com.neo4j.driver.kotlin.network.protocol.error.ProtocolOperationException
import com.neo4j.driver.kotlin.network.protocol.error.ProtocolStateException
import io.netty.channel.Channel
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandlerAdapter
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Future
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

class RequestManager : ChannelInboundHandlerAdapter() {

    private lateinit var channel: Channel

    private val lock = ReentrantLock()
    private val requests = mutableListOf<CompletableFuture<OperationResult<Boolean>>>()

    fun sendRequest(request: Any): Future<OperationResult<Boolean>> {
        val future = CompletableFuture<OperationResult<Boolean>>()

        this.lock.withLock {
            this.requests += future
            this.channel.writeAndFlush(request)
        }

        return future
    }

    override fun handlerAdded(ctx: ChannelHandlerContext) {
        this.channel = ctx.channel()
    }

    override fun channelRead(ctx: ChannelHandlerContext, msg: Any) {
        if (msg is SuccessMessage || msg is IgnoredMessage || msg is FailureMessage) {
            return this.processResponse(msg)
        }

        super.channelRead(ctx, msg)
    }

    override fun channelInactive(ctx: ChannelHandlerContext) {
        this.lock.withLock {
            this.requests.forEach { it.completeExceptionally(DriverConnectionException("Connection terminated")) }
        }

        super.channelInactive(ctx)
    }

    private fun processResponse(msg: Any) {
        val future = this.lock.withLock {
            this.requests.removeFirstOrNull()
                ?: throw ProtocolStateException("Received response without prior request")
        }

        when (msg) {
            is SuccessMessage -> future.complete(OperationResult(true, msg.metadata))
            is IgnoredMessage -> future.complete(OperationResult(false))
            is FailureMessage -> future.completeExceptionally(
                ProtocolOperationException(
                    msg.metadata.code,
                    msg.metadata.message
                )
            )
        }
    }
}