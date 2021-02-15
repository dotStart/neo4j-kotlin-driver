package com.neo4j.driver.kotlin.error

abstract class DriverException(message: String?, cause: Throwable?) : Exception(message, cause)