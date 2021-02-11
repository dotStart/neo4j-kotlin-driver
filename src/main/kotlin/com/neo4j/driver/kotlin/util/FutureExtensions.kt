package com.neo4j.driver.kotlin.util

import java.util.concurrent.ExecutionException
import java.util.concurrent.Future

/**
 * Awaits the result of a given future and returns it back to the caller.
 *
 * When an [ExecutionException] is thrown as a result of the future, it will be unwrapped (e.g. the cause is re-thrown).
 */
fun <V> Future<V>.getUnwrapped(): V {
    return try {
        this.get()
    } catch (ex: ExecutionException) {
        throw ex.cause ?: ex
    }
}