package com.neo4j.driver.kotlin.network.transport

import io.netty.channel.Channel
import io.netty.channel.EventLoopGroup
import io.netty.channel.socket.ServerSocketChannel
import io.netty.channel.socket.SocketChannel
import java.util.*
import java.util.concurrent.ThreadFactory
import kotlin.reflect.KClass

/**
 * Provides a factory for system specific network transport implementations.
 *
 * Implementations of this interface are expected to be registered with the Java Virtual Machine's [ServiceLoader]
 * facilities and will be automatically discovered at runtime based on the caller's context Class-Path as exposed
 * by [Thread.contextClassLoader].
 *
 * Selection of network transports is based on their respective [supported] flag (which indicates whether a given
 * transport is supported by the current execution environment) as well as its respective [priority] value (which
 * indicates its relative performance).
 */
interface NetworkTransport {

    /**
     * Defines a human readable name with which this particular transport is identified for the purposes of logging.
     */
    val name: String

    /**
     * Identifies whether this particular network transport is supported within the current execution environment.
     *
     * The value of this property typically depends on the current operating system as well as its respective
     * architecture.
     *
     * The default implementation of this property always returns `true` thus indicating support for all platforms
     * supported by the JVM. Most implementations will thus override this property.
     */
    val supported: Boolean
        get() = true

    /**
     * Identifies the relative priority with which this network transport is selected.
     *
     * This property is used as a deciding factor when multiple implementations are available within a given execution
     * environment. Generally speaking, implementations with a higher performance/throughput should be marked with a
     * higher priority value in order to make them more likely to be selected.
     *
     * The default implementation of this property always returns `0` indicating no special preference for this
     * particular transport implementation. Please note, that the fallback implementations will typically be registered
     * with a priority value of [Int.MIN_VALUE] in order to be the least likely to be selected in the face of multiple
     * implementations.
     */
    val priority: Int
        get() = 0

    /**
     * Identifies the channel type which is to be used when creating new server socket channels via this particular
     * transport.
     *
     * Note: The returned channel implementation is expected to be compatible with the event loop group returned via the
     * [createEventLoopGroup] factory method.
     */
    val serverSocketChannelType: KClass<out ServerSocketChannel>

    /**
     * Identifies the channel type which is to be used when creating new socket channels via this particular transport.
     *
     * Note: The returned channel implementation is expected to be compatible with the event loop group returned via the
     * [createEventLoopGroup] factory method.
     */
    val socketChannelType: KClass<out SocketChannel>

    /**
     * Identifies the channel type which is to be used when creating new datagram channels via this particular
     * transport.
     *
     * Note: The returned channel implementation is expected to be compatible with the event loop group returned via the
     * [createEventLoopGroup] factory method.
     */
    val datagramChannelType: KClass<out Channel>

    /**
     * Creates a new event loop group for use with the channel types returned by the properties within this
     * implementation.
     *
     * When [nThreads] is left on its default (`0`), the amount of logical processors within the current execution
     * environment is chosen as the amount of threads within the new thread pool.
     *
     * When no thread factory is given, a sensible default thread factory is chosen instead. The naming scheme for the
     * allocated threads is chosen using netty's internal default pattern.
     */
    fun createEventLoopGroup(nThreads: Int = 0, threadFactory: ThreadFactory? = null): EventLoopGroup

    companion object {

        /**
         * Exposes a listing of network transport implementations located within the application Class-Path.
         *
         * The return value of this property is dependant on the caller's respective context Class-Path as exposed via
         * the [Thread.contextClassLoader] property.
         */
        val installed: List<NetworkTransport>
            get() = ServiceLoader.load(NetworkTransport::class.java)
                .sortedByDescending { it.priority }
                .toList()

        /**
         * Exposes a listing of supported network transport implementations located within the application Class-Path.
         *
         * Similarly to [installed], the return value of this property is dependant on the caller's respective context
         * Class-Path.
         *
         * @see installed for a listing of all installed implementations.
         */
        val supported: List<NetworkTransport>
            get() = this.installed
                .filter { it.supported }

        /**
         * Retrieves the most optimal implementation of [NetworkTransport] within the caller Class-Path.
         *
         * Note: Typically this property is guaranteed to return a network transport implementation as a fallback
         * implementation is expected to be present within the application Class-Path.
         *
         * @throws IllegalStateException when no implementation is available within the application Class-Path.
         * @see NioNetworkTransport for the fallback implementation.
         */
        val optimal: NetworkTransport
            get() = this.supported
                .maxByOrNull { it.priority }
                ?: throw IllegalStateException("No supported implementation of NetworkTransport found")
    }
}