package com.neo4j.driver.kotlin.packstream

import com.neo4j.driver.kotlin.packstream.event.Visitor
import java.io.BufferedInputStream
import java.io.Closeable
import java.io.DataInputStream
import java.io.InputStream

/**
 * Provides functions for decoding Packstream structures from a given input stream.
 */
class PackstreamInputStream(source: InputStream) : Closeable by source, AutoCloseable {

    /**
     * For the purposes of reading common data structures as well as reverting the stream back to a previous position
     * at times, the target stream will be wrapped within [DataInputStream] and [BufferedInputStream] respectively.
     */
    private val delegate = DataInputStream(BufferedInputStream(source))

    /**
     * Decodes an arbitrary value within this stream and passes its contents to the given visitor.
     *
     * @throws IllegalStateException when an invalid marker byte is encountered
     */
    fun readValue(visitor: Visitor) {
        this.delegate.mark(1)

        val marker = try {
            this.delegate.read()
        } finally {
            this.delegate.reset()
        }

        when (marker) {
            in 0x00..0x7F, in 0xF0..0xFF, in 0xC8..0xCB -> visitor.visitInt(this.readInt())
            in 0x80..0x8F, in 0xD0..0xD2 -> visitor.visitString(this.readString())
            in 0x90..0x9F, in 0xD4..0xD6 -> this.readList(visitor)
            in 0xA0..0xAF, in 0xD8..0xDA -> this.readDictionary(visitor)
            in 0xB0..0xBF -> this.readStructure(visitor)
            0xC0 -> {
                this.readNull()
                visitor.visitNull()
            }
            0xC1 -> visitor.visitFloat(this.readFloat())
            in 0xC2..0xC3 -> visitor.visitBoolean(this.readBoolean())
            in 0xCC..0xCE -> visitor.visitBytes(this.readBytes())
            else -> throw IllegalStateException("Encountered invalid marker: 0x%02X".format(marker))
        }
    }

    /**
     * Decodes an integer value within this stream.
     *
     * @throws IllegalStateException when an invalid marker byte is encountered.
     */
    fun readInt(): Long = when (val marker = this.delegate.read()) {
//        in 0x00..0x7F -> marker.toLong()
//        in 0xF0..0xFF -> (marker - 0x100).toLong()
        in 0x00..0x7F, in 0xF0..0xFF -> marker.toLong()
        0xC8 -> this.delegate.read().toLong()
        0xC9 -> this.delegate.readShort().toLong()
        0xCA -> this.delegate.readInt().toLong()
        0xCB -> this.delegate.readLong()
        else -> throw IllegalStateException("Expected integer marker but got 0x%02X".format(marker))
    }

    private fun readStringLength() = when (val marker = this.delegate.read()) {
        in 0x80..0x8F -> marker and 0xF
        0xD0 -> this.delegate.readUnsignedByte()
        0xD1 -> this.delegate.readUnsignedShort()
        0xD2 -> this.delegate.readUnsignedShort()
        else -> throw IllegalStateException("Expected string marker but got 0x%02X".format(marker))
    }

    /**
     * Decodes a string value within this stream.
     *
     * @throws IllegalStateException when an invalid marker byte is encountered.
     */
    fun readString(): String {
        val length = this.readStringLength()

        return this.delegate.readNBytes(length)
            .toString(Charsets.UTF_8)
    }

    private fun readListLength() = when (val marker = this.delegate.read()) {
        in 0x90..0x9F -> marker and 0x0F
        0xD4 -> this.delegate.readUnsignedByte()
        0xD5 -> this.delegate.readUnsignedShort()
        0xD6 -> this.delegate.readInt()
        else -> throw IllegalStateException("Expected list marker but got 0x%02X".format(marker))
    }

    /**
     * Decodes a list value within this stream and passes its values to a given visitor.
     *
     * @throws IllegalStateException when an invalid marker byte is encountered.
     */
    fun readList(visitor: Visitor) {
        val length = this.readListLength()

        val childVisitor = visitor.visitList(length)
        repeat(length) {
            if (childVisitor != null) {
                this.readValue(childVisitor)
            } else {
                this.skipValue()
            }
        }
        visitor.visitListEnd()
    }

    private fun readDictionaryLength() = when (val marker = this.delegate.read()) {
        in 0xA0..0xAF -> marker and 0x0F
        0xD8 -> this.delegate.readUnsignedByte()
        0xD9 -> this.delegate.readUnsignedShort()
        0xDA -> this.delegate.readInt()
        else -> throw IllegalStateException("Expected dictionary marker but got 0x%02X".format(marker))
    }

    /**
     * Decodes a dictionary value within this stream and passes its values to a given visitor.
     *
     * @throws IllegalStateException when an invalid marker byte is encountered.
     */
    fun readDictionary(visitor: Visitor) {
        val length = this.readDictionaryLength()

        val childVisitor = visitor.visitDictionary(length)
        repeat(length) {
            if (childVisitor != null) {
                val key = this.readString()
                childVisitor.visitKey(key)
                this.readValue(childVisitor)
            } else {
                this.skipString()
                this.skipValue()
            }
        }
        visitor.visitDictionaryEnd()
    }

    private fun readStructureLength(): Int {
        val marker = this.delegate.read()
        val type = marker ushr 4 and 0x0F
        check(type == 0xB) { "Expected structure tag but got 0x%02X".format(type) }

        return marker and 0x0F
    }

    /**
     * Decodes a structure value within this stream and passes its values to a given visitor.
     *
     * @throws IllegalStateException when an invalid marker byte is encountered.
     */
    fun readStructure(visitor: Visitor) {
        val length = this.readStructureLength()
        val tag = this.delegate.read()

        val childVisitor = visitor.visitStructure(tag)
        repeat(length) {
            if (childVisitor != null) {
                this.readValue(childVisitor)
            } else {
                this.skipValue()
            }
        }
        visitor.visitStructureEnd()
    }

    /**
     * Decodes a null value within this stream.
     *
     * @throws IllegalStateException when an invalid marker byte is encountered.
     */
    fun readNull() {
        val marker = this.delegate.read()
        check(marker == 0xC0) { "Expected null tag but got 0x%02X".format(marker) }
    }

    /**
     * Decodes a float value within this stream.
     *
     * @throws IllegalStateException when an invalid marker byte is encountered.
     */
    fun readFloat(): Double {
        val marker = this.delegate.read()
        check(marker == 0xC1) { "Expected float tag but got 0x%02X".format(marker) }

        return this.delegate.readDouble()
    }

    /**
     * Decodes a boolean value within this stream.
     *
     * @throws IllegalStateException when an invalid marker byte is encountered.
     */
    fun readBoolean(): Boolean = when (val marker = this.delegate.read()) {
        0xC2 -> false
        0xC3 -> true
        else -> throw IllegalStateException("Expected boolean tag but got 0x%02X".format(marker))
    }

    private fun readBytesLength() = when (val marker = this.delegate.read()) {
        0xCC -> this.delegate.readUnsignedByte()
        0xCD -> this.delegate.readUnsignedShort()
        0xCE -> this.delegate.readInt()
        else -> throw IllegalStateException("Expected bytes tag but got 0x%02X".format(marker))
    }

    /**
     * Decodes a bytes value within this stream.
     */
    fun readBytes(): ByteArray {
        val length = this.readBytesLength()
        return this.delegate.readNBytes(length)
    }

    /**
     * Discard a single value within this stream.
     */
    fun skipValue() {
        this.delegate.mark(1)

        val marker = try {
            this.delegate.read()
        } finally {
            this.delegate.reset()
        }

        when (marker) {
            in 0x00..0x7F, in 0xF0..0xFF, in 0xC8..0xCB -> this.skipInt()
            in 0x80..0x8F, in 0xD0..0xD2 -> this.skipString()
            in 0x90..0x9F, in 0xD4..0xD6 -> this.skipList()
            in 0xA0..0xAF, in 0xD8..0xDA -> this.skipDictionary()
            in 0xB0..0xBF -> this.skipStructure()
            0xC0 -> this.skipNull()
            0xC1 -> this.skipFloat()
            in 0xC2..0xC3 -> this.skipBoolean()
            in 0xCC..0xCE -> this.skipBytes()
        }
    }

    /**
     * Discards a single integer value within this stream.
     */
    fun skipInt() {
        this.readInt()
    }

    /**
     * Discard a single string value within this stream.
     */
    fun skipString() {
        val length = this.readStringLength()
        this.delegate.skipNBytes(length.toLong())
    }

    /**
     * Discards a single list value within this stream.
     */
    fun skipList() {
        val length = this.readListLength()

        repeat(length) {
            this.skipValue()
        }
    }

    /**
     * Discards a single dictionary value within this stream.
     */
    fun skipDictionary() {
        val length = this.readDictionaryLength()

        repeat(length) {
            this.skipString()
            this.skipValue()
        }
    }

    /**
     * Discards a single structure value within this stream.
     */
    fun skipStructure() {
        val length = this.readStructureLength()
        this.delegate.skipNBytes(1)

        repeat(length) {
            this.skipValue()
        }
    }

    /**
     * Discards a single null value within this stream.
     *
     * This function is functionally equivalent to [readNull] but is included for semantic purposes.
     */
    fun skipNull() = this.readNull()

    /**
     * Discards a single float value within this stream.
     */
    fun skipFloat() {
        this.readFloat()
    }

    /**
     * Discards a single boolean value within this stream.
     */
    fun skipBoolean() {
        this.readBoolean()
    }

    /**
     * Discards a single byte array value within this stream.
     */
    fun skipBytes() {
        val length = this.readBytesLength()
        this.delegate.skipNBytes(length.toLong())
    }
}