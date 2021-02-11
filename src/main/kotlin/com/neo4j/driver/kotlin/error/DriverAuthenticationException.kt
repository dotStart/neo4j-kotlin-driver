package com.neo4j.driver.kotlin.error

/**
 * Notifies a caller about an issue related to the given authentication credentials.
 *
 * This exception is typically thrown when the given credentials are invalid or a different authentication method is
 * expected.
 */
class DriverAuthenticationException(message: String? = null, cause: Throwable? = null) :
    DriverConnectionException(message, cause)