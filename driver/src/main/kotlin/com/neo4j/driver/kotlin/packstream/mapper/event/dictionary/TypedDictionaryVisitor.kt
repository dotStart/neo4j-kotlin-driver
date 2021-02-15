package com.neo4j.driver.kotlin.packstream.mapper.event.dictionary

import com.neo4j.driver.kotlin.packstream.mapper.MappingFeature
import com.neo4j.driver.kotlin.packstream.mapper.annotation.PackstreamProperty
import com.neo4j.driver.kotlin.packstream.mapper.error.PackstreamMapperDeserializationException
import com.neo4j.driver.kotlin.packstream.mapper.event.AbstractDictionaryConstructingVisitor
import com.neo4j.driver.kotlin.packstream.mapper.event.ConstructingVisitor
import com.neo4j.driver.kotlin.packstream.mapper.event.DictionaryConstructingVisitor
import com.neo4j.driver.kotlin.packstream.mapper.event.MappingContext
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.full.findAnnotation

class TypedDictionaryVisitor<V : Any>(
    private val ctx: MappingContext,
    private val creatorFunc: KFunction<V>
) : AbstractDictionaryConstructingVisitor<V>() {

    private val parameterDefinitionMap = creatorFunc.parameters
        .map {
            val key = it.findAnnotation<PackstreamProperty>()
                ?.serializedName
                ?: it.name

            key to it
        }
        .toMap()

    private val parameterValueMap = mutableMapOf<KParameter, Any?>()

    private val parameter: KParameter
        get() = this.parameterDefinitionMap[this.key]
            ?: throw PackstreamMapperDeserializationException("Illegal parameter: No such parameter $key")

    override fun finalizeObject(): V {
        // fill in any remaining nullable parameters with null if permitted by the context
        if (MappingFeature.strictParameterPopulation !in this.ctx) {
            this.parameterDefinitionMap.values
                .filter { !it.isOptional }
                .filter { it.type.isMarkedNullable }
                .filter { it !in this.parameterValueMap }
                .forEach { this.parameterValueMap[it] = null }
        }

        // locate any remaining missing parameters and throw if they are required for the creator invocation
        val missingParameters = this.parameterDefinitionMap.values
            .filter { !it.isOptional }
            .filter { it !in this.parameterValueMap }
        if (missingParameters.isNotEmpty()) {
            val remainingNames = missingParameters
                .map { it.name }
                .joinToString(", ")

            throw PackstreamMapperDeserializationException("Incomplete object definition: Missing parameter(s): $remainingNames")
        }

        // parameters should all be populated at this point - catch any remaining exceptions produced by the creator
        // function and wrap them appropriately to comply with the specification
        return try {
            this.creatorFunc.callBy(this.parameterValueMap)
        } catch (ex: Exception) {
            throw PackstreamMapperDeserializationException("Failed to invoke creator function: ${creatorFunc.name}", ex)
        }
    }

    override fun storeEntry(value: Any?) {
        val parameter = this.parameter

        if (value == null && MappingFeature.strictNullHandling in this.ctx && !parameter.type.isMarkedNullable) {
            throw PackstreamMapperDeserializationException("Illegal value for $key: May not be null")
        }

        this.parameterValueMap[parameter] = value
    }

    override fun createListVisitor(length: Int): ConstructingVisitor<out Any> {
        return this.ctx.createListVisitor(this.parameter.type)
    }

    override fun createDictionaryListener(length: Int): DictionaryConstructingVisitor<out Any> {
        return this.ctx.createDictionaryVisitor(this.parameter.type)
    }

    override fun createStructureListener(length: Int): ConstructingVisitor<out Any> {
        return this.ctx.createStructureVisitor(this.parameter.type)
    }
}