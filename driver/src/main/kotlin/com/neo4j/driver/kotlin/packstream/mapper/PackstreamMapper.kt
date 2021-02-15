package com.neo4j.driver.kotlin.packstream.mapper

import com.neo4j.driver.kotlin.packstream.PackstreamInputStream
import com.neo4j.driver.kotlin.packstream.PackstreamOutputStream
import com.neo4j.driver.kotlin.packstream.mapper.annotation.PackstreamOrder
import com.neo4j.driver.kotlin.packstream.mapper.annotation.PackstreamProperty
import com.neo4j.driver.kotlin.packstream.mapper.annotation.PackstreamStructure
import com.neo4j.driver.kotlin.packstream.mapper.error.PackstreamMapperSerializationException
import com.neo4j.driver.kotlin.packstream.mapper.event.MappingContext
import com.neo4j.driver.kotlin.packstream.mapper.event.structure.RegistryTypedStructureVisitor
import com.neo4j.driver.kotlin.packstream.mapper.event.structure.ParameterizedTypedStructureVisitor
import com.neo4j.driver.kotlin.packstream.mapper.registry.StructureRegistry
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.hasAnnotation
import kotlin.reflect.full.memberProperties

/**
 * Provides mapping of Packstream structures to and from simple Kotlin objects.
 */
class PackstreamMapper private constructor(features: Map<MappingFeature<out Any?>, Any?>) {

    private val ctx = MappingContext(features)

    companion object {

        /**
         * Provides a Packstream mapper which assumes default values for all of its configuration options.
         */
        val default = PackstreamMapper(emptyMap())
    }

    /**
     * Decodes a structure of an arbitrary object type from the given source stream.
     */
    fun <V : Any> readStructure(inputStream: PackstreamInputStream, type: KClass<V>): V {
        val visitor = ParameterizedTypedStructureVisitor(this.ctx, type)
        inputStream.readStructure(visitor)
        return visitor.finalizeObject()
    }

    /**
     * Decodes a structure of an arbitrary object type from the given source stream.
     */
    fun readStructure(inputStream: PackstreamInputStream, registry: StructureRegistry): Any {
        val visitor = RegistryTypedStructureVisitor(this.ctx, registry)
        inputStream.readStructure(visitor)
        return visitor.finalizeObject()
    }

    /**
     * Writes an arbitrary object or value to the given target stream.
     */
    fun <V> writeValue(outputStream: PackstreamOutputStream, value: V) {
        when (value) {
            is Byte -> outputStream.writeInt(value)
            is Short -> outputStream.writeInt(value)
            is Int -> outputStream.writeInt(value)
            is Long -> outputStream.writeInt(value)

            is String -> outputStream.writeString(value)

            is List<*> -> this.writeList(outputStream, value)
            is Map<*, *> ->
                @Suppress("UNCHECKED_CAST")
                this.writeDictionary(outputStream, value as Map<Any?, Any?>)

            null -> outputStream.writeNull()

            is Float -> outputStream.writeFloat(value)
            is Double -> outputStream.writeFloat(value)

            is Boolean -> outputStream.writeBoolean(value)
            is ByteArray -> outputStream.writeBytes(value)

            else -> this.writeObject(outputStream, value)
        }
    }

    /**
     * Writes an arbitrary object to the given target stream.
     *
     * This function effectively encodes the given object either as a dictionary or structure depending on its
     * respective configuration.
     *
     * For the purposes of this implementation, types which have been annotated using the [PackstreamStructure]
     * annotation are encoded as structures while all remaining types are encoded as plain dictionaries.
     */
    fun <V : Any> writeObject(outputStream: PackstreamOutputStream, value: V) {
        if (value::class.hasAnnotation<PackstreamStructure>()) {
            return this.writeStructure(outputStream, value)
        }

        this.writeDictionary(outputStream, value)
    }

    /**
     * Writes an arbitrarily sized list to a given target stream.
     *
     * Lists may contain any type of value and are not technically restricted to a specific kind of values (e.g. value
     * types may be randomly combined). Please note, however, that decoding may require a specific type to be specified
     * as the mapper requires some kind of type information.
     */
    fun <V> writeList(outputStream: PackstreamOutputStream, value: List<V>) {
        outputStream.writeListHeader(value.size)
        value.forEach { this.writeValue(outputStream, it) }
    }

    /**
     * Writes an arbitrarily sized dictionary to a given target stream.
     *
     * For the purposes of this implementation, all properties will be written unless explicitly included. When no
     * property name for the purposes of serialization is specified via the [PackstreamProperty] annotation, its simple
     * name will be used instead.
     */
    fun <V : Any> writeDictionary(outputStream: PackstreamOutputStream, value: V) {
        val properties = value::class.memberProperties

        outputStream.writeDictionaryHeader(properties.size)
        properties
            .map {
                @Suppress("UNCHECKED_CAST")
                it as KProperty1<V, Any?>
            }
            .forEach { property ->
                val name = property.findAnnotation<PackstreamProperty>()
                    ?.serializedName
                    ?: property.name

                outputStream.writeDictionaryKey(name)
                this.writeValue(outputStream, property.get(value))
            }
    }

    /**
     * Writes an arbitrarily sized dictionary to a given target stream.
     *
     * While map keys may be of any type, they will be encoded as strings on the wire as the protocol does not permit
     * custom key types. Please also note, that while there are no restrictions on value types, decoding may require
     * a specific type to be set.
     */
    fun writeDictionary(outputStream: PackstreamOutputStream, value: Map<Any?, Any?>) {
        outputStream.writeDictionaryHeader(value.size)
        value.forEach { key, value ->
            outputStream.writeDictionaryKey(key.toString())
            this.writeValue(outputStream, value)
        }
    }

    /**
     * Writes an arbitrarily sized structure to a given target stream.
     *
     * Passed objects are expected to be annotated using the [PackstreamStructure] annotation in order to determine
     * their respective tag (e.g. their type identifier).
     *
     * @throws PackstreamMapperSerializationException when serialization fails or an invalid type is passed.
     */
    fun <V : Any> writeStructure(outputStream: PackstreamOutputStream, value: V) {
        val fieldCount = value::class.memberProperties
            .size
        val tag = value::class.findAnnotation<PackstreamStructure>()
            ?.tag
            ?: throw PackstreamMapperSerializationException("Cannot serialize ${value::class} as structure: Missing tag")

        outputStream.writeStructureHeader(fieldCount, tag)
        value::class.memberProperties
            .sortedBy {
                it.findAnnotation<PackstreamOrder>()
                    ?.value
                    ?: Int.MAX_VALUE
            }
            .map {
                @Suppress("UNCHECKED_CAST")
                it as KProperty1<V, Any>
            }
            .forEach { property ->
                this.writeValue(outputStream, property.get(value))
            }
    }
}