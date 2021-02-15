package com.neo4j.driver.kotlin.network.protocol.error

/**
 * Notifies a caller about an issue related to the protocol integrity on a given connection.
 *
 * This exception is typically thrown when a peer receives a message which is technically valid within the current
 * connection state but is not a valid response given the current request.
 */
class ProtocolViolationException(message: String? = null, cause: Throwable? = null) :
    ProtocolException(message, cause)