package com.neo4j.driver.kotlin.packstream.mapper

/**
 * Provides a listing of configurable mapping related features.
 *
 * Mapping features configure various aspects of the (de-)serialization process and may significantly alter the library
 * behavior. All features are assigned a sensible default value.
 */
class MappingFeature<V>(

    /**
     * Defines the default value which shall be used when no value is given during mapper construction.
     */
    val default: V
) {

    companion object {

        /**
         * Configures whether a mapping exception is raised when an unknown property is encountered while deserializing
         * a Packstream structure.
         */
        val ignoreUnknownProperties = MappingFeature(true)

        /**
         * Configures whether a mapping exception is raised when a `null` value is passed to a property which may not be
         * set to null but has a default value.
         *
         * When disabled, non-nullable properties will be assigned their respective default value when `null` is
         * received.
         */
        val strictNullHandling = MappingFeature(true)

        /**
         * Configures whether a mapping exception is raised when a property permits `null` values but has not been
         * explicitly populated while deserializing a Packstream structure.
         */
        val strictParameterPopulation = MappingFeature(true)
    }
}