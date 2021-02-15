package com.neo4j.driver.kotlin.error

open class DriverConnectionException(message: String? = null, cause: Throwable? = null) :
    DriverException(message, cause)