package com.neo4j.driver.kotlin.network.packet

import com.neo4j.driver.kotlin.util.ProtocolVersion

/**
 * Represents a client handshake.
 *
 * This message is sent by clients as an introductory message when the connection is first established and effectively
 * negotiates a protocol version by passing a list consisting of four locally supported revisions.
 *
 * Servers will respond with a [ServerHandshakePacket].
 */
data class ClientHandshakePacket(
    val magicNumber: Int = defaultMagicNumber,
    val supportedVersions: List<ProtocolVersion>
) {

    companion object {

        /**
         * Defines the standard magic number which is transmitted when none is given explicitly.
         */
        const val defaultMagicNumber = 0x6060B017
    }

    init {
        require(supportedVersions.size <= 4) { "Expected 4 supported revisions or less" }
        require(supportedVersions.none { it == ProtocolVersion.unsupported }) { "Version 0 is reserved for protocol purposes" }
    }
}