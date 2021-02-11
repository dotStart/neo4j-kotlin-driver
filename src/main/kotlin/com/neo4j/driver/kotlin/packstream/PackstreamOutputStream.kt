package com.neo4j.driver.kotlin.packstream

import java.io.Closeable
import java.io.DataOutputStream
import java.io.OutputStream

/**
 * Provides functions for encoding Packstream structures.
 */
class PackstreamOutputStream(target: OutputStream) : Closeable by target, AutoCloseable {

    private val delegate = DataOutputStream(target)

    /**
     * Byte alias for [writeInt].
     */
    fun writeInt(value: Byte) = this.writeInt(value.toLong())

    /**
     * Short alias for [writeInt].
     */
    fun writeInt(value: Short) = this.writeInt(value.toLong())

    /**
     * Int alias for [writeInt].
     */
    fun writeInt(value: Int) = this.writeInt(value.toLong())

    /**
     * Encodes an arbitrarily sized signed integer into the underlying stream.
     */
    fun writeInt(value: Long) {
        when (value) {
            in -16..127 -> this.delegate.write(value.toInt())
            in Byte.MIN_VALUE..Byte.MAX_VALUE -> {
                this.delegate.write(0xC8)
                this.delegate.write(value.toInt())
            }
            in Short.MIN_VALUE..Short.MAX_VALUE -> {
                this.delegate.write(0xC9)
                this.delegate.writeShort(value.toInt())
            }
            in Int.MIN_VALUE..Int.MAX_VALUE -> {
                this.delegate.write(0xCA)
                this.delegate.writeInt(value.toInt())
            }
            else -> {
                this.delegate.write(0xCB)
                this.delegate.writeLong(value)
            }
        }
    }

    /**
     * Encodes an arbitrarily sized string into the underlying stream.
     */
    fun writeString(value: String) {
        val encoded = value.toByteArray(Charsets.UTF_8)

        when (encoded.size) {
            in 0..15 -> this.delegate.write(0x80 or encoded.size)
            in 16..255 -> {
                this.delegate.write(0xD0)
                this.delegate.write(encoded.size)
            }
            in 256..65_535 -> {
                this.delegate.write(0xD1)
                this.delegate.writeShort(encoded.size)
            }
            else -> {
                this.delegate.writeShort(0xD2)
                this.delegate.writeInt(encoded.size)
            }
        }

        this.delegate.write(encoded)
    }

    /**
     * Encodes a list header into the underlying stream.
     */
    fun writeListHeader(length: Int) {
        when (length) {
            in 0..15 -> this.delegate.write(0x90 or length)
            in 16..255 -> {
                this.delegate.write(0xD4)
                this.delegate.write(length)
            }
            in 256..65_535 -> {
                this.delegate.write(0xD5)
                this.delegate.writeShort(length)
            }
            else -> {
                this.delegate.writeShort(0xD6)
                this.delegate.writeInt(length)
            }
        }
    }

    /**
     * Encodes a dictionary header into the underlying stream.
     */
    fun writeDictionaryHeader(length: Int) {
        when (length) {
            in 0..15 -> this.delegate.write(0xA0 or length)
            in 16..255 -> {
                this.delegate.write(0xD8)
                this.delegate.write(length)
            }
            in 256..65_535 -> {
                this.delegate.write(0xD9)
                this.delegate.writeShort(length)
            }
            else -> {
                this.delegate.writeShort(0xDA)
                this.delegate.writeInt(length)
            }
        }
    }

    /**
     * Encodes an arbitrary dictionary key into the underlying stream.
     *
     * This function is functionally equal to [writeString] but is provided for the purposes of semantic completeness.
     */
    fun writeDictionaryKey(key: String) = this.writeString(key)

    /**
     * Encodes a structure header into the underlying stream.
     */
    fun writeStructureHeader(length: Int, tag: Int) {
        require(length >= 0) { "Structure length cannot be negative" }
        require(length < 16) { "Structure length cannot exceed 15 fields" }
        require(tag >= 0) { "Structure tag cannot be negative" }
        require(tag < 128) { "Structure tag cannot exceed 127" }

        this.delegate.write(0xB0 or length)
        this.delegate.write(tag)
    }

    /**
     * Encodes a null value into the underlying stream.
     */
    fun writeNull() {
        this.delegate.write(0xC0)
    }

    /**
     * Float version of [writeFloat].
     */
    fun writeFloat(value: Float) = this.writeFloat(value.toDouble())

    /**
     * Encodes an arbitrarily sized signed floating point value into the underlying stream.
     */
    fun writeFloat(value: Double) {
        this.delegate.write(0xC1)
        this.delegate.writeDouble(value)
    }

    /**
     * Encodes a boolean value into the underlying stream.
     */
    fun writeBoolean(value: Boolean) {
        this.delegate.write(
            if (value) {
                0xC2
            } else {
                0xC3
            }
        )
    }

    /**
     * Encodes an arbitrarily sized byte array into the underlying stream.
     */
    fun writeBytes(value: ByteArray) {
        when (value.size) {
            in 0..255 -> {
                this.delegate.write(0xCC)
                this.delegate.write(value.size)
            }
            in 256..65_535 -> {
                this.delegate.write(0xCD)
                this.delegate.writeShort(value.size)
            }
            else -> {
                this.delegate.writeShort(0xCE)
                this.delegate.writeInt(value.size)
            }
        }

        this.delegate.write(value)
    }
}