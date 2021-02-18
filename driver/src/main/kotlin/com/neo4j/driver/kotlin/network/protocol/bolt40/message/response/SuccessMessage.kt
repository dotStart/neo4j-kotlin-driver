package com.neo4j.driver.kotlin.network.protocol.bolt40.message.response

import com.neo4j.driver.kotlin.packstream.mapper.annotation.PackstreamStructure

/**
 * Notifies a client about a successful execution related to a previous request.
 */
@PackstreamStructure(0x70)
data class SuccessMessage(

    /**
     * Provides additional information related to the command execution.
     *
     * For instance, this field will be populated with information regarding the remote server implementation in
     * response to the Hello command.
     */
    val metadata: Map<String, Any?>
)