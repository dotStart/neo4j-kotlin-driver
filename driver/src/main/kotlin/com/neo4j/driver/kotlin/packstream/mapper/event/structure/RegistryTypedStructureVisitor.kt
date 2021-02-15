package com.neo4j.driver.kotlin.packstream.mapper.event.structure

import com.neo4j.driver.kotlin.packstream.event.Visitor
import com.neo4j.driver.kotlin.packstream.mapper.error.PackstreamMapperDeserializationException
import com.neo4j.driver.kotlin.packstream.mapper.event.MappingContext
import com.neo4j.driver.kotlin.packstream.mapper.event.ConstructingVisitor
import com.neo4j.driver.kotlin.packstream.mapper.registry.StructureRegistry

class RegistryTypedStructureVisitor(
    private val ctx: MappingContext,
    private val registry: StructureRegistry
) : ConstructingVisitor<Any> {

    private var delegate: ConstructingVisitor<out Any>? = null

    override fun finalizeObject(): Any {
        val delegate = this.delegate
            ?: throw PackstreamMapperDeserializationException("Invalid operand order: Nothing read")

        return delegate.finalizeObject()
    }

    override fun visitStructure(tag: Int): Visitor {
        val targetType = this.registry[tag]
            ?: throw PackstreamMapperDeserializationException("Invalid structure tag: Unknown type 0x%02X".format(tag))

        val visitor = this.ctx.createStructureVisitor(targetType)
        this.delegate = visitor
        return visitor
    }
}