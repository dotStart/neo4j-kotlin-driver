package com.neo4j.driver.kotlin.network

import com.neo4j.driver.kotlin.network.handler.util.SuspendingMessageHandler
import io.netty.channel.Channel
import java.util.concurrent.CancellationException
import kotlin.reflect.KClass

/**
 * Waits for a message of a given type to be received by the desired channel.
 *
 * This method is primarily used as a utility when preforming synchronous tasks on a given channel (such as
 * handshaking).
 */
fun <M : Any> Channel.await(type: KClass<M>, initializer: (Channel) -> Unit = {}): M {
    val handler = SuspendingMessageHandler(type)

    this.pipeline()
        .addLast(handler)

    initializer(this)

    try {
        return handler.await()
    } catch (ex: CancellationException) {
        this.pipeline()
            .remove(handler)

        throw ex
    }
}

inline fun <reified M : Any> Channel.await(noinline initializer: (Channel) -> Unit = {}): M {
    return this.await(M::class, initializer)
}