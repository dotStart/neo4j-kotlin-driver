package com.neo4j.driver.kotlin.packstream.mapper.registry

import kotlin.reflect.KClass

/**
 * Provides a registry which maps structures to their respective tag values within the protocol.
 */
interface StructureRegistry {

    /**
     * Retrieves a structure type based on its respective registered structure tag.
     *
     * When no type with the given structure is present within this registry, null is returned instead.
     */
    operator fun get(tag: Int): KClass<out Any>?
}