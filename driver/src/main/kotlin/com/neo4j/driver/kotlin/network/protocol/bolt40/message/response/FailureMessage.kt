package com.neo4j.driver.kotlin.network.protocol.bolt40.message.response

import com.neo4j.driver.kotlin.packstream.mapper.annotation.PackstreamStructure

/**
 * Notifies a client about a failure to comply with a given prior request.
 *
 * This message typically refers to an actual client or server side issue which lead to the failure of a previously
 * issued request.
 */
@PackstreamStructure(0x7F)
data class FailureMessage(val metadata: Metadata) {

    data class Metadata(

        /**
         * Provides a standardized error code which identifies the category of the problem which lead to the failure of
         * the command.
         */
        val code: String,

        /**
         * Provides additional human readable information about the error.
         */
        val message: String
    )
}