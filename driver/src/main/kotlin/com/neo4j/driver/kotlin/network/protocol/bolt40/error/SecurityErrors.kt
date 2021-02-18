package com.neo4j.driver.kotlin.network.protocol.bolt40.error

import com.neo4j.driver.kotlin.network.protocol.error.ErrorCategory.SECURITY
import com.neo4j.driver.kotlin.network.protocol.error.ErrorClassification.CLIENT_ERROR
import com.neo4j.driver.kotlin.network.protocol.error.ErrorCode

/**
 * Provides a listing of security related errors within the 4.2 protocol revision.
 */
object SecurityErrors {
    val authenticationRateLimit = ErrorCode(CLIENT_ERROR, SECURITY, "AuthenticationRateLimit")
    val authorizedExpired = ErrorCode(CLIENT_ERROR, SECURITY, "AuthorizationExpired")
    val credentialsExpired = ErrorCode(CLIENT_ERROR, SECURITY, "CredentialsExpired")
    val forbidden = ErrorCode(CLIENT_ERROR, SECURITY, "Forbidden")
    val unauthorized = ErrorCode(CLIENT_ERROR, SECURITY, "Unauthorized")
}