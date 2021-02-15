package com.neo4j.driver.kotlin.network.codec

import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.ByteToMessageDecoder

/**
 * Decodes message frames from a chunked input stream.
 *
 * For the purposes of this implementation, chunks are prefixed with an unsigned 16-bit header which identifies the
 * total length of any given chunk. Empty chunks (with a size of zero), are used as markers to indicate the end of a
 * message.
 */
class ChunkedFrameDecoder : ByteToMessageDecoder() {

    override fun decode(ctx: ChannelHandlerContext, input: ByteBuf, out: MutableList<Any>) {
        while (input.isReadable(2)) {
            // immediately mark the current position within our input buffer in order to revert to it if the current
            // message is not yet available in its entirety
            input.markReaderIndex()

            var messageSize = 0
            while (true) {
                // each chunk is prefixed by a 16-bit length header which identifies the amount of data immediately
                // following the header - a chunk size of zero indicates the end of the message thus requiring us to
                // keep accumulating data until the end of a chunk is encountered
                if (!input.isReadable(2)) {
                    input.resetReaderIndex()
                    return
                }

                val chunkSize = input.readUnsignedShort()

                // if the chunk size has been set to zero, we have reached the end of the message and are ready to pass
                // it on for further decoding within the pipeline
                if (chunkSize == 0) {
                    break
                }

                // if a chunk has not yet been received completely (most likely due to additional fragmentation
                // somewhere along the network path), we will discard all state and wait for new data to arrive
                if (!input.isReadable(chunkSize)) {
                    input.resetReaderIndex()
                    return
                }

                messageSize += chunkSize
                input.skipBytes(chunkSize)
            }

            // we have received a full message with a terminating zero length chunk and may thus reset to the beginning
            // of the buffer in order to collect its elements into a single buffer
            input.resetReaderIndex()
            val heap = ctx.alloc().directBuffer(messageSize)

            while (true) {
                val chunkSize = input.readUnsignedShort()
                if (chunkSize == 0) {
                    break
                }

                input.readBytes(heap, chunkSize)
            }

            out.add(heap)
        }
    }
}