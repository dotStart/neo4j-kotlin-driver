package com.neo4j.driver.kotlin.packstream.mapper.registry

import com.neo4j.driver.kotlin.packstream.mapper.annotation.PackstreamStructure
import kotlin.reflect.KClass
import kotlin.reflect.full.findAnnotation

/**
 * Provides an immutable structure registry implementation.
 *
 * This is the default implementation which is typically used in cases where all structure types are known ahead of time
 * and never change throughout the lifetime of the application.
 */
class DefaultStructureRegistry private constructor(
    private val structures: Map<Int, KClass<out Any>>
) : StructureRegistry {

    override fun get(tag: Int): KClass<out Any>? = this.structures[tag]

    companion object {

        /**
         * Creates a new structure registry with a given set of structure registrations.
         */
        fun create(block: Factory.() -> Unit): DefaultStructureRegistry {
            val factory = Factory()
            factory.block()
            return factory.build()
        }
    }

    class Factory {
        private val structures = mutableMapOf<Int, KClass<out Any>>()

        /**
         * Constructs a new structure registry using the structures present within this builder at the current time.
         *
         * Resulting structure registries are immutable and will use a snapshot of the registrations present within
         * this factory instance at the time of the function call. Subsequent changes to the factory will not affect
         * previously constructed registry instances.
         */
        fun build() = DefaultStructureRegistry(this.structures.toMap())

        /**
         * Shorthand function for [build].
         *
         * @see build for a full documentation on the function behavior.
         */
        operator fun invoke() = this.build()

        /**
         * Registers a new structure with this factory.
         */
        fun register(type: KClass<out Any>) {
            val annotation = type.findAnnotation<PackstreamStructure>()
                ?: throw IllegalArgumentException("Invalid structure type: @PackstreamStructure annotation is missing")

            require(annotation.tag !in this.structures) {
                val registeredType = this.structures[annotation.tag]
                "Structure tag clash: ${annotation.tag} has previously been registered to type $registeredType"
            }

            this.structures[annotation.tag] = type
        }

        /**
         * Shorthand for [register].
         *
         * @see register for a full documentation on this function.
         */
        operator fun plusAssign(type: KClass<out Any>) = this.register(type)
    }
}