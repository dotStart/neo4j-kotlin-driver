package com.neo4j.driver.kotlin.packstream.mapper.annotation

/**
 * Marks an annotated constructor as the primary constructor for the purposes of Packstream deserialization.
 */
@MustBeDocumented
@Target(AnnotationTarget.CONSTRUCTOR)
@Retention(AnnotationRetention.RUNTIME)
annotation class PackstreamCreator