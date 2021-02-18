package com.neo4j.driver.kotlin.network.protocol.bolt40.message.request.streaming

import com.neo4j.driver.kotlin.packstream.mapper.annotation.PackstreamProperty
import com.neo4j.driver.kotlin.packstream.mapper.annotation.PackstreamStructure

@PackstreamStructure(0x3F)
data class PullMessage(
    val parameters: Parameters = Parameters()
) {

    data class Parameters(
        val n: Int = -1,
        @PackstreamProperty("qid") // TODO: unnecessary - documentation purposes only
        val queryId: Int = -1
    )
}