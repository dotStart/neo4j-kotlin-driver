package com.neo4j.driver.kotlin.packstream.mapper.annotation

/**
 * Marks an annotated class as a structure.
 *
 * Annotated classes may be directly en- and decoded via the [StructureMapper] to/from their respective representations
 * within the Packstream format.
 */
@MustBeDocumented
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class PackstreamStructure(

    /**
     * Identifies the tag (also sometimes referred to as "signature") with which this particular structure is identified
     * within the resulting Packstream encoding.
     */
    val tag: Int
)
