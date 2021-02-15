package com.neo4j.driver.kotlin.network.codec

import com.neo4j.driver.kotlin.packstream.PackstreamInputStream
import com.neo4j.driver.kotlin.packstream.mapper.PackstreamMapper
import com.neo4j.driver.kotlin.packstream.mapper.registry.StructureRegistry
import io.netty.buffer.ByteBuf
import io.netty.buffer.ByteBufInputStream
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.ByteToMessageDecoder

class PackstreamMessageDecoder(
    private val mapper: PackstreamMapper,
    private val registry: StructureRegistry
) : ByteToMessageDecoder() {

    override fun decode(ctx: ChannelHandlerContext, input: ByteBuf, out: MutableList<Any>) {
        PackstreamInputStream(ByteBufInputStream(input))
            .use {
                out.add(this.mapper.readStructure(it, this.registry))
            }
    }
}