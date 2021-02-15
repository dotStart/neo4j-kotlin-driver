package com.neo4j.driver.kotlin.util

import java.util.concurrent.ThreadFactory
import kotlin.concurrent.thread

/**
 * Constructs threads with an ever increasing index number and base name format.
 */
class NumberedFormatThreadFactory(private val format: String) : ThreadFactory {

    private var index = 0;

    override fun newThread(r: Runnable) = thread(
        name = format.format(this.index++),
        start = false,
        block = r::run
    )
}