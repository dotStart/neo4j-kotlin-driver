package com.neo4j.driver.kotlin.network.codec

import com.neo4j.driver.kotlin.util.ProtocolVersion
import com.neo4j.driver.kotlin.network.packet.ServerHandshakePacket
import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.ByteToMessageDecoder

class ServerHandshakeDecoder : ByteToMessageDecoder() {

    override fun decode(ctx: ChannelHandlerContext, input: ByteBuf, out: MutableList<Any>) {
        // packet fragmentation is not expected within this context, we'll account for it anyways just in case
        if (!input.isReadable(4)) {
            return
        }

        val negotiatedVersion = ProtocolVersion(input.readInt())
        out.add(ServerHandshakePacket(negotiatedVersion))
    }
}