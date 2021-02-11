package com.neo4j.driver.kotlin.network.transport

import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.nio.NioDatagramChannel
import io.netty.channel.socket.nio.NioServerSocketChannel
import io.netty.channel.socket.nio.NioSocketChannel
import java.util.concurrent.ThreadFactory

/**
 * Provides a NIO ("New IO") based network transport implementation which is available on all implementations of the
 * Java Virtual Machine.
 *
 * This implementation acts as a fallback implementation for systems where no other native transport is made available
 * by the netty maintainers yet (typically Windows and certain architectures of the Linux and BSD kernels).
 *
 * Note: This implementation is also given the priority value of [Int.MIN_VALUE] in order to prevent its selection when
 * more suitable implementations are made available for the current execution environment.
 */
class NioNetworkTransport : NetworkTransport {

    override val name = "NIO"

    override val priority
        get() = Int.MIN_VALUE

    override val serverSocketChannelType = NioServerSocketChannel::class
    override val socketChannelType = NioSocketChannel::class
    override val datagramChannelType = NioDatagramChannel::class

    override fun createEventLoopGroup(nThreads: Int, threadFactory: ThreadFactory?) =
        NioEventLoopGroup(nThreads, threadFactory)
}