package com.neo4j.driver.kotlin.packstream.mapper.event

import com.neo4j.driver.kotlin.packstream.mapper.MappingFeature
import com.neo4j.driver.kotlin.packstream.mapper.annotation.PackstreamCreator
import com.neo4j.driver.kotlin.packstream.mapper.error.PackstreamMapperDeserializationException
import com.neo4j.driver.kotlin.packstream.mapper.event.dictionary.TypedDictionaryVisitor
import com.neo4j.driver.kotlin.packstream.mapper.event.dictionary.WildcardDictionaryVisitor
import com.neo4j.driver.kotlin.packstream.mapper.event.list.TypedListVisitor
import com.neo4j.driver.kotlin.packstream.mapper.event.structure.TypedStructureVisitor
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KType
import kotlin.reflect.full.hasAnnotation
import kotlin.reflect.full.primaryConstructor
import kotlin.reflect.jvm.jvmErasure

class MappingContext(
    private val features: Map<MappingFeature<out Any?>, Any?>
) {

    private fun <T : Any> getConstructorFor(type: KClass<T>): KFunction<T> =
        type.constructors
            .find { it.hasAnnotation<PackstreamCreator>() }
            ?: type.primaryConstructor
            ?: throw PackstreamMapperDeserializationException("No constructor for type: $type")

    fun createDictionaryVisitor(type: KType): DictionaryConstructingVisitor<out Any> {
        return when (type.jvmErasure) {
            Map::class -> {
                if (type.arguments.size != 2) {
                    throw PackstreamMapperDeserializationException("Illegal type reference for Map: Expected exactly two arguments")
                }

                val keyType = type.arguments[0]
                    .type
                    ?: throw PackstreamMapperDeserializationException("Illegal type reference for Map: Cannot extract key type")
                val valueType = type.arguments[1]
                    .type
                    ?: throw PackstreamMapperDeserializationException("Illegal type reference for Map: Cannot extract value type")

                if (keyType.jvmErasure != String::class) {
                    throw PackstreamMapperDeserializationException("Illegal type reference for Map: Expected key to be of type String but got $keyType")
                }
                if (valueType.jvmErasure != Any::class) {
                    TODO("Support for type restrictions within maps has not been implemented yet")
                }

                WildcardDictionaryVisitor()
            }
            else -> this.createDictionaryVisitor(type.jvmErasure)
        }
    }

    fun <T : Any> createDictionaryVisitor(type: KClass<T>): DictionaryConstructingVisitor<out T> {
        return TypedDictionaryVisitor(this, this.getConstructorFor(type))
    }

    fun createListVisitor(type: KType): ConstructingVisitor<out Any> {
        return TypedListVisitor(this, type)
    }

    fun createStructureVisitor(type: KType): ConstructingVisitor<out Any> {
        return this.createStructureVisitor(type.jvmErasure)
    }

    fun <T : Any> createStructureVisitor(type: KClass<T>): ConstructingVisitor<out T> {
        return TypedStructureVisitor(this, this.getConstructorFor(type))
    }

    /**
     * Retrieves the configuration value of a given mapping feature.
     *
     * When no value has been configured for the given feature, its respective default (as exposed
     * by [MappingFeature.default]) will be chosen instead.
     */
    @Suppress("UNCHECKED_CAST")
    operator fun <V> get(feature: MappingFeature<V>): V {
        if (feature in this.features) {
            return this.features[feature] as V
        }
        return feature.default
    }

    /**
     * Evaluates whether a given boolean mapping feature has been enabled within this context.
     */
    operator fun contains(feature: MappingFeature<Boolean>): Boolean {
        return this[feature]
    }
}