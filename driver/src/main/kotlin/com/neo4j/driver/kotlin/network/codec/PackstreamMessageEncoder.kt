package com.neo4j.driver.kotlin.network.codec

import com.neo4j.driver.kotlin.packstream.PackstreamOutputStream
import com.neo4j.driver.kotlin.packstream.mapper.PackstreamMapper
import com.neo4j.driver.kotlin.packstream.mapper.annotation.PackstreamStructure
import io.netty.buffer.ByteBuf
import io.netty.buffer.ByteBufOutputStream
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.MessageToByteEncoder
import kotlin.reflect.full.hasAnnotation

/**
 * Encodes plain old Kotlin objects to their respective Packstream representations.
 *
 * This encoder will discard any objects which do not qualify for structure based encoding and push them back into the
 * pipeline for further processing within another handler further down the stream.
 */
class PackstreamMessageEncoder(
    private val mapper: PackstreamMapper
) : MessageToByteEncoder<Any>() {

    override fun acceptOutboundMessage(msg: Any): Boolean {
        // ignore any non structure objects which would cause an exception to be thrown anyways - handling of those
        // objects may be provided by a different handler within the pipeline
        return super.acceptOutboundMessage(msg) && msg::class.hasAnnotation<PackstreamStructure>()
    }

    override fun encode(ctx: ChannelHandlerContext, msg: Any, out: ByteBuf) {
        PackstreamOutputStream(ByteBufOutputStream(out)) // TODO: Remove wrapper
            .use {
                this.mapper.writeStructure(it, msg)
            }
    }
}