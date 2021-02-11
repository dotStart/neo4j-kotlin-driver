package com.neo4j.driver.kotlin.packstream.mapper.annotation

/**
 * Specifies the order in which an annotated property is to be serialized into the Packstream when the
 */
@MustBeDocumented
@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.RUNTIME)
annotation class PackstreamOrder(

    /**
     * Identifies the order in which this property is encoded.
     */
    val value: Int
)
