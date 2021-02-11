package com.neo4j.driver.kotlin.network.codec

import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.MessageToByteEncoder
import java.lang.Integer.min

/**
 * Encodes message frames with a given target chunk size.
 *
 * Messages which exceed the given chunk size (defaulting to the maximum permitted size in accordance with the wire
 * protocol) will be split up into multiple separate chunks until no data remains. Smaller chunk sizes may reduce
 * latency but add additional processing overhead.
 *
 * @see ChunkedFrameDecoder for more information on chunking.
 */
class ChunkedFrameEncoder(private val chunkSize: Int = defaultChunkSize) : MessageToByteEncoder<ByteBuf>() {

    companion object {

        /**
         * Identifies the maximum permitted chunk size to be transmitted via the wire.
         */
        const val maxChunkSize = 65536

        /**
         * Identifies the default chunk size which is chosen when no value is given.
         */
        const val defaultChunkSize = maxChunkSize
    }

    init {
        require(this.chunkSize <= maxChunkSize) { "Chunk size cannot exceed $maxChunkSize bytes" }
    }

    override fun encode(ctx: ChannelHandlerContext, msg: ByteBuf, out: ByteBuf) {
        if (msg.readableBytes() < this.chunkSize) {
            // if the given message is shorter than the configured chunk size, we'll be able to simply write it to the
            // wire as-is in addition to the chunk header
            out.writeShort(msg.readableBytes())
            out.writeBytes(msg)
        } else {
            while (msg.isReadable) {
                val length = min(msg.readableBytes(), this.chunkSize)
                out.writeShort(length)
                out.writeBytes(msg, length)
            }
        }

        // write an empty terminator chunk to indicate the end of the respective message
        out.writeShort(0)
    }
}