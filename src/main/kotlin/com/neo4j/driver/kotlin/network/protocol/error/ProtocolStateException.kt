package com.neo4j.driver.kotlin.network.protocol.error

/**
 * Notifies a caller about an issue related to the current protocol state.
 *
 * This exception is most commonly thrown when a message is received out of context (e.g. when it was not expected).
 */
class ProtocolStateException(message: String? = null, cause: Throwable? = null) : ProtocolException(message, cause)