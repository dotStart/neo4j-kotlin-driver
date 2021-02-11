package com.neo4j.driver.kotlin.packstream.event

interface Visitor {

    fun visitNull() = Unit

    fun visitBoolean(value: Boolean) = Unit

    fun visitInt(value: Long) = Unit

    fun visitFloat(value: Double) = Unit

    fun visitBytes(value: ByteArray) = Unit

    fun visitString(value: String) = Unit

    fun visitList(length: Int): Visitor? = null

    fun visitListEnd() = Unit

    fun visitDictionary(length: Int): DictionaryVisitor? = null

    fun visitDictionaryEnd() = Unit

    fun visitStructure(tag: Int): Visitor? = null

    fun visitStructureEnd() = Unit
}