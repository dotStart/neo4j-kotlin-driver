package tv.dotstart.neo4j.kotlin.testkit.message.command

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonTypeName

@JsonTypeName("SessionRun")
data class SessionRunCommand(
    val sessionId: String,
    val cypher: String,
    @JsonProperty("params")
    val parameters: Map<String, Any?>?,
    @JsonProperty("txMeta")
    val transactionMetadata: Map<String, Any?>?,
    val timeout: Long?
) : CommandParameters