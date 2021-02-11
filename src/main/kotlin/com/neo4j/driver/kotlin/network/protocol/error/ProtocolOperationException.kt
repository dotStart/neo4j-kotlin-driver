package com.neo4j.driver.kotlin.network.protocol.error

/**
 * Notifies a caller about an issue related to their respective request.
 */
class ProtocolOperationException(
    val code: String,
    val description: String,
    cause: Throwable? = null
) : ProtocolException(
    buildString {
        append("[")
        append(code)
        append("] ")
        append(description)
    },
    cause
)