package com.neo4j.driver.kotlin.network.protocol.error

import com.neo4j.driver.kotlin.error.DriverException

/**
 * Notifies a client about an issue related to a protocol problem.
 */
abstract class ProtocolException(message: String? = null, cause: Throwable? = null) : DriverException(message, cause)