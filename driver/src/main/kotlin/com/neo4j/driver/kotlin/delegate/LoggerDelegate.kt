package com.neo4j.driver.kotlin.delegate

import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

/**
 * Provides a delegate which automatically acquires a logger instance for a given object.
 *
 * When the enclosing object is a companion object, it's respective enclosure will be used as the logger target.
 */
class LoggerDelegate : AbstractLazyReadOnlyProperty<Any, Logger>() {

    override fun initialize(thisRef: Any): Logger {
        val targetType = thisRef::class
            .takeIf { it.isCompanion }
            ?.java
            ?.enclosingClass
            ?: thisRef::class.java

        return LogManager.getLogger(targetType)
    }
}

/**
 * Shorthand constructor function for [LoggerDelegate] instances.
 */
fun log4j() = LoggerDelegate()