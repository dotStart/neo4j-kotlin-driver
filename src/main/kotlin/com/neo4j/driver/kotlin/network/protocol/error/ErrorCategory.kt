package com.neo4j.driver.kotlin.network.protocol.error

enum class ErrorCategory(val encoded: String) {
    SECURITY("Security"),

    UNKNOWN("");

    companion object {

        private val encodedMap = values()
            .map { it.encoded to it }
            .toMap()

        /**
         * Retrieves a given error category based on its respective category.
         */
        operator fun get(encoded: String): ErrorCategory? = this.encodedMap[encoded]
    }
}