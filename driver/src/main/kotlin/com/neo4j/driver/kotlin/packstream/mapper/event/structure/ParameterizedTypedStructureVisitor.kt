package com.neo4j.driver.kotlin.packstream.mapper.event.structure

import com.neo4j.driver.kotlin.packstream.event.Visitor
import com.neo4j.driver.kotlin.packstream.mapper.event.MappingContext
import com.neo4j.driver.kotlin.packstream.mapper.event.ConstructingVisitor
import kotlin.reflect.KClass

class ParameterizedTypedStructureVisitor<V : Any>(
    private val ctx: MappingContext,
    type: KClass<V>
) : ConstructingVisitor<V> {

    private var delegate = this.ctx.createStructureVisitor(type)

    override fun finalizeObject(): V = this.delegate.finalizeObject()

    override fun visitStructure(tag: Int): Visitor = this.delegate
}