package com.neo4j.driver.kotlin.network.protocol.error

enum class ErrorClassification(val encoded: String) {
    CLIENT_ERROR("ClientError"),
    CLIENT_NOTIFICATION("ClientNotification"),

    TRANSIENT_ERROR("TransientError"),

    DATABASE_ERROR("DatabaseError"),

    UNKNOWN("");

    companion object {

        private val encodedMap = values()
            .map { it.encoded to it }
            .toMap()

        /**
         * Retrieves a given error classification based on its respective category.
         */
        operator fun get(encoded: String): ErrorClassification? = this.encodedMap[encoded]
    }
}