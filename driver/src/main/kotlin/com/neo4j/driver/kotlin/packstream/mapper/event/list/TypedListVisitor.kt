package com.neo4j.driver.kotlin.packstream.mapper.event.list

import com.neo4j.driver.kotlin.packstream.mapper.error.PackstreamMapperDeserializationException
import com.neo4j.driver.kotlin.packstream.mapper.event.AbstractConstructingVisitor
import com.neo4j.driver.kotlin.packstream.mapper.event.ConstructingVisitor
import com.neo4j.driver.kotlin.packstream.mapper.event.DictionaryConstructingVisitor
import com.neo4j.driver.kotlin.packstream.mapper.event.MappingContext
import kotlin.reflect.KType
import kotlin.reflect.jvm.jvmErasure

class TypedListVisitor(
    private val ctx: MappingContext,
    private val type: KType,
) : AbstractConstructingVisitor<MutableList<Any?>>() {

    private val content = mutableListOf<Any?>()

    override fun finalizeObject() = this.content.toMutableList()

    override fun storeEntry(value: Any?) {
        if (value == null) {
            if (!this.type.isMarkedNullable) {
                throw PackstreamMapperDeserializationException("Illegal value: List does not accept null values")
            }
        } else if (!this.type.jvmErasure.isInstance(value)) {
            throw PackstreamMapperDeserializationException("Illegal value: List does not accept values of type ${value::class}")
        }

        this.content += value
    }

    override fun visitInt(value: Long) {
        this.content += when (this.type.jvmErasure) {
            Byte::class -> value.toByte()
            Short::class -> value.toShort()
            Int::class -> value.toInt()
            else -> value
        }
    }

    override fun createListVisitor(length: Int): ConstructingVisitor<out Any> {
        TODO("Nested lists are not supported at the moment")
    }

    override fun createDictionaryListener(length: Int): DictionaryConstructingVisitor<out Any> {
        TODO("Nested dictionaries are not supported at the moment")
    }

    override fun createStructureListener(length: Int): ConstructingVisitor<out Any> {
        TODO("Nested structures are not supported at the moment")
    }

    override fun visitFloat(value: Double) {
        this.content += when (this.type.jvmErasure) {
            Float::class -> value.toFloat()
            else -> value
        }
    }
}