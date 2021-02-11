package com.neo4j.driver.kotlin.packstream.mapper.event.dictionary

import com.neo4j.driver.kotlin.packstream.mapper.error.PackstreamMapperDeserializationException
import com.neo4j.driver.kotlin.packstream.mapper.event.AbstractDictionaryConstructingVisitor
import com.neo4j.driver.kotlin.packstream.mapper.event.ConstructingVisitor
import com.neo4j.driver.kotlin.packstream.mapper.event.DictionaryConstructingVisitor
import com.neo4j.driver.kotlin.packstream.mapper.event.list.WildcardListVisitor

class WildcardDictionaryVisitor : AbstractDictionaryConstructingVisitor<Map<String, Any?>>() {

    private val content = mutableMapOf<String, Any?>()

    override fun finalizeObject(): Map<String, Any?> = this.content.toMap()

    override fun storeEntry(value: Any?) {
        this.content[this.key] = value
    }

    override fun createListVisitor(length: Int): ConstructingVisitor<out Any> {
        return WildcardListVisitor();
    }

    override fun createDictionaryListener(length: Int): DictionaryConstructingVisitor<out Any> {
        return WildcardDictionaryVisitor()
    }

    override fun createStructureListener(length: Int): ConstructingVisitor<out Any> {
        // TODO: Structure registry
        throw PackstreamMapperDeserializationException("Illegal value: Untyped dictionaries cannot contain structures")
    }
}