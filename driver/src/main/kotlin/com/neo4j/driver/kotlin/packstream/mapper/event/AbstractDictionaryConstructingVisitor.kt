package com.neo4j.driver.kotlin.packstream.mapper.event

import com.neo4j.driver.kotlin.packstream.mapper.error.PackstreamMapperDeserializationException

abstract class AbstractDictionaryConstructingVisitor<V : Any> : AbstractConstructingVisitor<V>(),
    DictionaryConstructingVisitor<V> {

    private var _key: String? = null

    /**
     * Identifies the key which was visited most recently.
     *
     * When no key has been visited prior to accessing this property, an exception is thrown instead.
     */
    protected val key: String
        get() = this._key
            ?: throw PackstreamMapperDeserializationException("Illegal operation order: Expected dictionary entry key")

    override fun validateState() {
        super.validateState()

        if (this._key == null) {
            throw PackstreamMapperDeserializationException("Illegal operation order: Expected dictionary entry key")
        }
    }

    override fun resetState() {
        super.resetState()

        this._key = null
    }

    override fun visitKey(key: String) {
        this._key = key
    }
}