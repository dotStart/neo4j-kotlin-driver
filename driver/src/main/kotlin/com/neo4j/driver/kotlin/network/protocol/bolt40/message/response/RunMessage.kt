package com.neo4j.driver.kotlin.network.protocol.bolt40.message.response

import com.neo4j.driver.kotlin.packstream.mapper.annotation.PackstreamOrder
import com.neo4j.driver.kotlin.packstream.mapper.annotation.PackstreamStructure

@PackstreamStructure(0x10)
data class RunMessage(
    @PackstreamOrder(0)
    val query: String,
    @PackstreamOrder(1)
    val parameters: Map<String, Any?> = emptyMap(),
    @PackstreamOrder(2)
    val extra: ExtraParameters = ExtraParameters()
) {

    data class ExtraParameters(
        // TODO: Bookmarks and stuff
        val db: String = ""
    )
}