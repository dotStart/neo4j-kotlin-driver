package com.neo4j.driver.kotlin.network.codec

import com.neo4j.driver.kotlin.network.packet.ClientHandshakePacket
import com.neo4j.driver.kotlin.util.ProtocolVersion
import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.ByteToMessageDecoder

/**
 * Provides decoding capabilities for Bolt handshaking packets.
 *
 * Each packet is comprised of a total of 20 bytes allocated as follows:
 *
 *  - Magic Number (0x60 0x60 0xB0 0x17) - 4 Bytes
 *  - (4x) Supported Version - 4 Bytes each
 */
class ClientHandshakeDecoder : ByteToMessageDecoder() {

    override fun decode(ctx: ChannelHandlerContext, input: ByteBuf, out: MutableList<Any>) {
        // fragmenting at this level is pretty unlikely but we'll account for it anyways just in case something silly
        // happens
        if (!input.isReadable(4 + (4 * 4))) {// 4 bytes magic number + 4 * 4 bytes version numbers
            return
        }

        val magicNumber = input.readInt()
        val supportedVersions = (0 until 4)
            .map { input.readInt() }
            .map { ProtocolVersion(it) }
            .toList()

        out += ClientHandshakePacket(magicNumber, supportedVersions)
    }
}