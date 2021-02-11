package com.neo4j.driver.kotlin.packstream.mapper.event.structure

import com.neo4j.driver.kotlin.packstream.mapper.MappingFeature
import com.neo4j.driver.kotlin.packstream.mapper.error.PackstreamMapperDeserializationException
import com.neo4j.driver.kotlin.packstream.mapper.event.AbstractConstructingVisitor
import com.neo4j.driver.kotlin.packstream.mapper.event.ConstructingVisitor
import com.neo4j.driver.kotlin.packstream.mapper.event.DictionaryConstructingVisitor
import com.neo4j.driver.kotlin.packstream.mapper.event.MappingContext
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.jvm.jvmErasure

class TypedStructureVisitor<V : Any>(
    private val ctx: MappingContext,
    private val creatorFunc: KFunction<V>
) : AbstractConstructingVisitor<V>() {

    private val remainingParameterDefinitions = creatorFunc.parameters.toMutableList()
    private val parameterValueMap = mutableMapOf<KParameter, Any?>()

    private var childVisitor: ConstructingVisitor<out Any>? = null

    override fun finalizeObject(): V {
        // remove any remaining optional parameters from the list as they will simply be initialized using their
        // respective defaults when omitted
        this.remainingParameterDefinitions.removeIf { it.isOptional }

        // fill in any remaining nullable parameters with null if permitted by the context
        if (MappingFeature.strictParameterPopulation !in this.ctx) {
            val nullParameters = this.remainingParameterDefinitions
                .filter { it.type.isMarkedNullable }

            nullParameters.forEach { this.parameterValueMap[it] = null }

            this.remainingParameterDefinitions.removeAll(nullParameters)
        }

        // ensure that no parameters remain to be filled - if so, we'll have to raise an exception as the construction
        // would otherwise fail
        if (this.remainingParameterDefinitions.isNotEmpty()) {
            val remainingNames = this.remainingParameterDefinitions
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
        val parameter = this.remainingParameterDefinitions.removeFirstOrNull()
            ?: throw PackstreamMapperDeserializationException("Illegal structure parameter: Parameter list exhausted")

        if (!parameter.type.jvmErasure.isInstance(value)) {
            throw PackstreamMapperDeserializationException("Illegal parameter value for ${parameter.name}: Expected ${parameter.type} but got ${value?.let { it::class }}")
        }

        this.parameterValueMap[parameter] = value
    }

    override fun createListVisitor(length: Int): ConstructingVisitor<out Any> {
        val parameter = this.remainingParameterDefinitions.firstOrNull()
            ?: throw PackstreamMapperDeserializationException("Illegal structure parameter: Parameter list exhausted")

        return this.ctx.createListVisitor(parameter.type)
    }

    override fun createDictionaryListener(length: Int): DictionaryConstructingVisitor<out Any> {
        val parameter = this.remainingParameterDefinitions.firstOrNull()
            ?: throw PackstreamMapperDeserializationException("Illegal structure parameter: Parameter list exhausted")

        return this.ctx.createDictionaryVisitor(parameter.type)
    }

    override fun createStructureListener(length: Int): ConstructingVisitor<out Any> {
        val parameter = this.remainingParameterDefinitions.firstOrNull()
            ?: throw PackstreamMapperDeserializationException("Illegal structure parameter: Parameter list exhausted")

        return this.ctx.createStructureVisitor(parameter.type.jvmErasure)
    }
}