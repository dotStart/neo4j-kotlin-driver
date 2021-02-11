package com.neo4j.driver.kotlin.packstream.mapper.error

/**
 * Notifies a caller about an issue related to the mapping process of a Packstream object.
 */
abstract class PackstreamMapperException(message: String? = null, cause: Throwable? = null) : Exception(message, cause)