package com.neo4j.driver.kotlin.network.protocol.bolt42.message.request.streaming

import com.neo4j.driver.kotlin.packstream.mapper.annotation.PackstreamStructure

@PackstreamStructure(0x71)
data class RecordMessage(
    val content: List<Any?>
)