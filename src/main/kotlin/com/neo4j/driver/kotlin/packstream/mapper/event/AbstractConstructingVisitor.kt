package com.neo4j.driver.kotlin.packstream.mapper.event

import com.neo4j.driver.kotlin.packstream.event.DictionaryVisitor
import com.neo4j.driver.kotlin.packstream.event.Visitor
import com.neo4j.driver.kotlin.packstream.mapper.error.PackstreamMapperDeserializationException

abstract class AbstractConstructingVisitor<V : Any> : ConstructingVisitor<V> {

    private var childVisitor: ConstructingVisitor<out Any>? = null

    /**
     * Validates whether the visitor is in a state where values may be processed.
     */
    protected open fun validateState() = Unit

    /**
     * Stores a given value within the visitor state.
     *
     * This function typically caches values until [finalizeObject] is explicitly invoked in order to produce a full
     * object from the current visitor state.
     */
    protected abstract fun storeEntry(value: Any?)

    /**
     * Performs a post action once a given value has been processed.
     */
    protected open fun resetState() = Unit

    /**
     * Constructs a new visitor for parsing lists nested within the context of this visitor.
     */
    protected abstract fun createListVisitor(length: Int): ConstructingVisitor<out Any>

    /**
     * Constructs a new visitor for parsing dictionaries nested within the context of this visitor.
     */
    protected abstract fun createDictionaryListener(length: Int): DictionaryConstructingVisitor<out Any>

    /**
     * Constructs a new visitor for parsing structures nested within the context of this visitor.
     */
    protected abstract fun createStructureListener(length: Int): ConstructingVisitor<out Any>

    override fun visitNull() {
        this.validateState()
        this.storeEntry(null)
        this.resetState()
    }

    override fun visitBoolean(value: Boolean) {
        this.validateState()
        this.storeEntry(value)
        this.resetState()
    }

    override fun visitInt(value: Long) {
        this.validateState()
        this.storeEntry(value)
        this.resetState()
    }

    override fun visitFloat(value: Double) {
        this.validateState()
        this.storeEntry(value)
        this.resetState()
    }

    override fun visitBytes(value: ByteArray) {
        this.validateState()
        this.storeEntry(value)
        this.resetState()
    }

    override fun visitString(value: String) {
        this.validateState()
        this.storeEntry(value)
        this.resetState()
    }

    /**
     * Handles final processing of nested value types such as dictionaries, lists and structures.
     */
    private fun visitChildEnd() {
        val childVisitor = this.childVisitor
            ?: throw PackstreamMapperDeserializationException("Illegal operator order: Expected child")

        this.storeEntry(childVisitor.finalizeObject())
        this.resetState()
    }

    override fun visitList(length: Int): Visitor? {
        this.validateState()

        val childVisitor = this.createListVisitor(length)
        this.childVisitor = childVisitor
        return childVisitor
    }

    override fun visitListEnd() = this.visitChildEnd()

    override fun visitDictionary(length: Int): DictionaryVisitor? {
        this.validateState()

        val childVisitor = this.createDictionaryListener(length)
        this.childVisitor = childVisitor
        return childVisitor
    }

    override fun visitDictionaryEnd() = this.visitChildEnd()

    override fun visitStructure(tag: Int): Visitor? {
        this.validateState()

        val childVisitor = this.createStructureListener(tag)
        this.childVisitor = childVisitor
        return childVisitor
    }

    override fun visitStructureEnd() = this.visitChildEnd()
}