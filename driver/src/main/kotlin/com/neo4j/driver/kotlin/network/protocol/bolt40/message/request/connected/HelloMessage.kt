package com.neo4j.driver.kotlin.network.protocol.bolt40.message.request.connected

import com.neo4j.driver.kotlin.packstream.mapper.annotation.PackstreamProperty
import com.neo4j.driver.kotlin.packstream.mapper.annotation.PackstreamStructure

/**
 * Introduces a client on a freshly established connection and optionally performs authentication.
 */
@PackstreamStructure(0x01)
data class HelloMessage(val parameters: Parameters) {

    data class Parameters(

        /**
         * Provides a human readable string with which the client implementation identifies itself.
         *
         * This value will typically also contain information such as the client version number and/or a URL pointing
         * towards the project website.
         */
        @PackstreamProperty("user_agent")
        val userAgent: String,

        /**
         * Identifies the authentication scheme with which the client desires to establish its privileges on the
         * database system.
         *
         * Typically this will be either of `basic` or `none` but other authentication schemes may be passed if desired.
         */
        val scheme: String,

        /**
         * Identifies the principal which wishes to authenticate.
         *
         * This value depends on the chosen authentication [scheme].
         */
        val principal: String?,

        /**
         * Identifies the credentials with which the principal's identity is to be verified.
         *
         * This value depends on the chosen authentication [scheme].
         */
        val credentials: String?
    )
}