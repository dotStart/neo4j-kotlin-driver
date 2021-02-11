package com.neo4j.driver.kotlin.packstream.mapper.event

import com.neo4j.driver.kotlin.packstream.event.Visitor
import com.neo4j.driver.kotlin.packstream.mapper.error.PackstreamMapperDeserializationException

interface ConstructingVisitor<V : Any> : Visitor {

    /**
     * Constructs a new object based on the properties this visitor has received throughout its lifetime.
     *
     * @throws IllegalStateException when the finalize method has previously been invoked on this particular object.
     * @throws PackstreamMapperDeserializationException when the construction fails due to lacking dependencies.
     */
    fun finalizeObject(): V
}