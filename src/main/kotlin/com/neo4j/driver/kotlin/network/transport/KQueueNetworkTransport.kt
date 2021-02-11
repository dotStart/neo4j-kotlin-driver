package com.neo4j.driver.kotlin.network.transport

import io.netty.channel.kqueue.*
import java.util.concurrent.ThreadFactory

/**
 * Provides a network transport implementation which makes use of FreeBSD's `KQueue` network functions.
 *
 * This implementation is restricted to environments which are based on FreeBSD and is currently only provided for
 * 64-bit instances of MacOS.
 */
class KQueueNetworkTransport : NetworkTransport {

    override val name = "KQueue"

    override val supported: Boolean
        get() = KQueue.isAvailable()

    override val serverSocketChannelType = KQueueServerSocketChannel::class
    override val socketChannelType = KQueueSocketChannel::class
    override val datagramChannelType = KQueueDatagramChannel::class

    override fun createEventLoopGroup(nThreads: Int, threadFactory: ThreadFactory?) =
        KQueueEventLoopGroup(nThreads, threadFactory)
}