package com.neo4j.driver.kotlin.network.protocol

import com.neo4j.driver.kotlin.delegate.log4j
import com.neo4j.driver.kotlin.util.ProtocolVersion
import io.netty.channel.Channel
import java.io.Closeable
import java.util.*

/**
 * Represents a connection to a given database server.
 *
 * The capabilities of connections may differ slightly depending on the respective level of support as indicated by
 * the [version] property. Logic which is otherwise not supported by the underlying protocol is typically replicated
 * as closely as possible. When this fallback logic is not possible, an [UnsupportedOperationException] is raised
 * instead.
 */
abstract class BoltProtocol(protected val channel: Channel) : Closeable, AutoCloseable {

    /**
     * Identifies the protocol version with which this connection communicates.
     *
     * @see Factory.version
     */
    abstract val version: ProtocolVersion

    /***
     * Performs a protocol level handshake with the remote side.
     *
     * This method is invoked once the connection has been established and a protocol version has been negotiated in
     * order to perform a protocol version specific handshake with the remote side. As such, it is primarily used in
     * order to initialize the server side state machine for future use.
     */
    abstract fun handshake(scheme: String = "none", principal: String? = null, credentials: String? = null)

    /**
     * Executes a query on a remote database.
     */
    abstract fun run(query: String, parameters: Map<String, Any?>): List<Map<String, Any?>>

    /**
     * Evaluates whether this particular connection remains valid in its current state.
     *
     * This method should be invoked prior to calling any other functions when connections remain stale for longer
     * periods of time (as is the case when connection pooling is used) as idle connections may be terminated by outside
     * factors such as load balancers or network monitoring systems.
     *
     * Note: The default implementation of this method will simply evaluate whether the underlying channel remains open
     * but will not attempt to write data or check the responsiveness of the server instance. Implementations should
     * extend this function in order to invoke a simple command for the purposes of validation as connections may be
     * reported as intact by the operating system until actually used to transmit data (in which case the transmitted
     * packets may be rejected as the link has become invalid).
     */
    open fun validate(): Boolean {
        return this.channel.isOpen
    }

    /**
     * Permanently closes this connection.
     *
     * Note: Connections may not be reused thus requiring the negotiation and creation of an entirely new connections
     * to a given database server. As such, connections should only ever be freed when closing the driver instance or
     * switching to a different database server.
     */
    override fun close() {
        this.channel.close()
            .await()
    }

    /**
     * Handles the construction of bolt connections around a given communication channel.
     *
     * Implementations of this interface are to be registered with Java's [ServiceLoader] facilities and will thus be
     * discovered automatically at runtime (thus permitting the loading of additional implementations by adding them
     * to the application Class-Path).
     *
     * Registered implementations will be listed within their respective order of priority (as described in the
     * documentation of the [priority] property).
     */
    interface Factory {

        /**
         * Identifies the protocol version with which this connection implementation communicates.
         *
         * This version number will be transmitted within the original connection handshake in order to negotiate its
         * use with the server side.
         */
        val version: ProtocolVersion

        /**
         * Identifies the relative priority with which this particular factory is selected.
         *
         * This priority value has no meaning of its own but rather is to be considered in relation to other
         * implementations provided by the application Class-Path. When multiple implementations are available the top
         * four implementations in accordance with their priority value are chosen. If the priority value matches, the
         * version with the highest respective implementation version is proposed with preference in order to provide
         * the largest subset of features.
         *
         * When more than four revisions are available, the remaining implementations are discarded.
         *
         * The default implementation of this property simply returns zero (implying no special precedence over other
         * implementations within the application Class-Path).
         */
        // TODO: Concat versions based on major revision
        val priority: Int
            get() = 0

        /**
         * Wraps the given connection with this Bolt protocol revision.
         */
        fun create(ch: Channel): BoltProtocol
    }

    companion object {
        private val logger by log4j()

        /**
         * Retrieves a listing of bolt connection implementations which are currently installed within the caller's
         * application Class-Path.
         *
         * The Class-Path used for the purposes of resolving implementations is based on the current Thread class path
         * as exposed via [Thread.contextClassLoader].
         *
         * Note: This property does not employ any caching mechanics thus potentially introducing some latency due to
         * resolving the service registrations within the caller Class-Path. Callers should generally cache the results
         * of this lookup where applicable.
         */
        val installed: List<Factory>
            get() = ServiceLoader.load(Factory::class.java, Thread.currentThread().contextClassLoader)
                .sortedWith { a, b ->
                    val priorityComparison = a.priority.compareTo(b.priority)
                    if (priorityComparison != 0) {
                        -priorityComparison
                    } else {
                        -a.version.compareTo(b.version)
                    }
                }
                .toList()
    }
}