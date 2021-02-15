package com.neo4j.driver.kotlin.network.protocol.bolt42.message.request.ready

import com.neo4j.driver.kotlin.packstream.mapper.annotation.PackstreamStructure

/**
 * Notifies the server that the client intends to gracefully terminate a connection.
 */
@PackstreamStructure(0x02)
class GoodbyeMessage