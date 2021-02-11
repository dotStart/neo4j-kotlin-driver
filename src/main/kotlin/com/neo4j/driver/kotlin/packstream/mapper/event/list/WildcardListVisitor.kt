package com.neo4j.driver.kotlin.packstream.mapper.event.list

import com.neo4j.driver.kotlin.packstream.mapper.error.PackstreamMapperDeserializationException
import com.neo4j.driver.kotlin.packstream.mapper.event.AbstractConstructingVisitor
import com.neo4j.driver.kotlin.packstream.mapper.event.ConstructingVisitor
import com.neo4j.driver.kotlin.packstream.mapper.event.DictionaryConstructingVisitor
import com.neo4j.driver.kotlin.packstream.mapper.event.dictionary.WildcardDictionaryVisitor

class WildcardListVisitor : AbstractConstructingVisitor<List<Any?>>() {

    private val content = mutableListOf<Any?>()

    override fun finalizeObject(): List<Any?> = this.content.toList()

    override fun storeEntry(value: Any?) {
        this.content += value
    }

    override fun createListVisitor(length: Int): ConstructingVisitor<out Any> {
        return WildcardListVisitor()
    }

    override fun createDictionaryListener(length: Int): DictionaryConstructingVisitor<out Any> {
        return WildcardDictionaryVisitor()
    }

    override fun createStructureListener(length: Int): ConstructingVisitor<out Any> {
        // TODO: Structure registry
        throw PackstreamMapperDeserializationException("Illegal value: Untyped lists cannot contain structures")
    }
}