package com.neo4j.driver.kotlin.network.protocol.bolt42.message.request.ready

import com.neo4j.driver.kotlin.packstream.mapper.annotation.PackstreamStructure

/**
 * Instructs the server to discard all state, abort any executing operations and revert the connection back to its ready
 * state.
 *
 * This message is also used as an interrupt message which cancels all running operations and discards all remaining
 * requests within the queue up to this point.
 */
@PackstreamStructure(0x0F)
class ResetMessage