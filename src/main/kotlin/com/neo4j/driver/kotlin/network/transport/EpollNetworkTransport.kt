package com.neo4j.driver.kotlin.network.transport

import io.netty.channel.epoll.*
import java.util.concurrent.ThreadFactory

/**
 * Provides a network transport which makes use of the `epoll` functions provided by the Linux kernel.
 *
 * This network transport is restricted to Linux environments and is currently only made available for 64-bit versions
 * of the Linux kernel.
 */
class EpollNetworkTransport : NetworkTransport {

    override val name = "epoll"

    override val supported: Boolean
        get() = Epoll.isAvailable()

    override val serverSocketChannelType = EpollServerSocketChannel::class
    override val socketChannelType = EpollSocketChannel::class
    override val datagramChannelType = EpollDatagramChannel::class

    override fun createEventLoopGroup(nThreads: Int, threadFactory: ThreadFactory?) =
        EpollEventLoopGroup(nThreads, threadFactory)
}