package com.neo4j.driver.kotlin.network.handler.handshake

import com.neo4j.driver.kotlin.network.codec.ClientHandshakeEncoder
import com.neo4j.driver.kotlin.network.codec.ServerHandshakeDecoder
import io.netty.channel.Channel
import io.netty.channel.ChannelInitializer

/**
 * Initializes a connection for client communication purposes.
 */
class ClientNegotationInitializationHandler : ChannelInitializer<Channel>() {

    override fun initChannel(ch: Channel) {
        ch.pipeline()
            .addLast("clientHandshakeEncoder", ClientHandshakeEncoder())
            .addLast("serverHandshakeDecoder", ServerHandshakeDecoder())
    }
}