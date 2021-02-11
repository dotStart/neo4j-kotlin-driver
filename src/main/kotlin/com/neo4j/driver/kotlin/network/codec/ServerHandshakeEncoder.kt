package com.neo4j.driver.kotlin.network.codec

import com.neo4j.driver.kotlin.network.packet.ServerHandshakePacket
import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.MessageToByteEncoder

class ServerHandshakeEncoder : MessageToByteEncoder<ServerHandshakePacket>() {

    override fun encode(ctx: ChannelHandlerContext, msg: ServerHandshakePacket, out: ByteBuf) {
        out.writeInt(msg.negotiatedVersion.value)
    }
}