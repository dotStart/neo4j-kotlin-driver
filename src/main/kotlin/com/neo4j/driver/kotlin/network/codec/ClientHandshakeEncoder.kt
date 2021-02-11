package com.neo4j.driver.kotlin.network.codec

import com.neo4j.driver.kotlin.network.packet.ClientHandshakePacket
import com.neo4j.driver.kotlin.util.ProtocolVersion
import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.MessageToByteEncoder

/**
 * @see ClientHandshakeDecoder for message structure
 */
class ClientHandshakeEncoder : MessageToByteEncoder<ClientHandshakePacket>() {

    override fun encode(ctx: ChannelHandlerContext, msg: ClientHandshakePacket, out: ByteBuf) {
        out.writeInt(msg.magicNumber)

        (0 until 4)
            .map {
                if (it < msg.supportedVersions.size) {
                    msg.supportedVersions[it]
                } else {
                    ProtocolVersion.padding
                }
            }
            .map { it.value }
            .forEach { out.writeInt(it) }
    }
}