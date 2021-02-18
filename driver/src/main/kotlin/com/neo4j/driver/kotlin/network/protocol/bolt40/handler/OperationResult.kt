package com.neo4j.driver.kotlin.network.protocol.bolt40.handler

data class OperationResult<V>(

    /**
     * Exposes the actual result of a given operation.
     */
    val value: V,

    /**
     * Exposes additional metadata as returned by the server.
     */
    val metadata: Map<String, Any?> = emptyMap()
)