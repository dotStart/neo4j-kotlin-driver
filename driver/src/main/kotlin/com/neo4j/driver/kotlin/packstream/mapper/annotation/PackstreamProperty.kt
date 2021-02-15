package com.neo4j.driver.kotlin.packstream.mapper.annotation

/**
 * Adjusts the dictionary serialization parameters of an annotated property.
 */
@MustBeDocumented
@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.RUNTIME)
annotation class PackstreamProperty(

    /**
     * Adjusts the name with which the annotated property is identified when serialized within a Packstream dictionary.
     */
    val serializedName: String
)
