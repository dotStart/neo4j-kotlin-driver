package com.neo4j.driver.kotlin.network.protocol.error

data class ErrorCode(
    val classification: ErrorClassification,
    val category: ErrorCategory,
    val title: String
) {

    /**
     * Provides an encoded representation of this error code as is transmitted via the wire in response to requests.
     */
    val encoded: String = buildString {
        append(prefix)

        append(classification.encoded)
        append(componentDelimiter)
        append(category.encoded)
        append(componentDelimiter)
        append(title)
    }

    companion object {

        private const val prefix = "Neo."
        private const val componentDelimiter = '.'
    }

    operator fun contains(code: String): Boolean {
        // TODO: Wildcards?
        return this.encoded == code
    }
}