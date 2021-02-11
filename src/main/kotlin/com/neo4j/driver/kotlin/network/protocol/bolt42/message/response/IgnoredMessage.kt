package com.neo4j.driver.kotlin.network.protocol.bolt42.message.response

import com.neo4j.driver.kotlin.packstream.mapper.annotation.PackstreamStructure

/**
 * Notifies a client about the fact that a given prior request has been ignored due to a failure in a previous command.
 *
 * This message implies that a given command has not been executed and its outcome is unknown. Clients should retry the
 * respective command (assuming their respective preconditions are still met) as a response to this command once the
 * failure state has been cleared.
 */
@PackstreamStructure(0x7E)
class IgnoredMessage