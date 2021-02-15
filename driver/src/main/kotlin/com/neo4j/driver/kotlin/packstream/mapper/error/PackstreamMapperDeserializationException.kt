package com.neo4j.driver.kotlin.packstream.mapper.error

/**
 * Notifies a caller about a deserialization issue within the mapper.
 *
 * This exception is most commonly thrown when a validation constraint within the mapper is violated (for instance, when
 * an unknown property is encountered or a required property is not populated via the Packstream structure).
 */
class PackstreamMapperDeserializationException(message: String? = null, cause: Throwable? = null) :
    PackstreamMapperException(message, cause)