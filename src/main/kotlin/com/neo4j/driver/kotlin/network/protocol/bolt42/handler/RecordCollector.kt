package com.neo4j.driver.kotlin.network.protocol.bolt42.handler

import com.neo4j.driver.kotlin.network.protocol.bolt42.message.request.streaming.RecordMessage
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandlerAdapter

class RecordCollector : ChannelInboundHandlerAdapter() {

    private val _content = mutableListOf<List<Any?>>()

    val content: List<List<Any?>>
        get() = this._content.toList()

    override fun channelRead(ctx: ChannelHandlerContext, msg: Any) {
        if (msg !is RecordMessage) {
            return super.channelRead(ctx, msg)
        }

        this._content += msg.content
    }
}