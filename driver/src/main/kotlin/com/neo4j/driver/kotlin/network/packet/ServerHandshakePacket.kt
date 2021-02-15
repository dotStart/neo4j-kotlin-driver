package com.neo4j.driver.kotlin.network.packet

import com.neo4j.driver.kotlin.util.ProtocolVersion

/**
 * Identifies the negotiated protocol revision which is to be used for all following communication.
 *
 * This message is transmitted by servers in response to [ClientHandshakePacket]s. Typically, it will mirror one of the
 * previously presented supported protocol revisions. Failing that, zero is transmitted in order to indicate failure
 * during the negotiation (e.g. none of the presented version numbers is accepted by the server implementation).
 *
 * Connections enter normal operation within their respective protocol revision following this message.
 */
data class ServerHandshakePacket(
    val negotiatedVersion: ProtocolVersion
)