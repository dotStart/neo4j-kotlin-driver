package tv.dotstart.neo4j.kotlin.testkit.message.response

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonTypeName

@JsonTypeName("BackendError")
data class BackendErrorResponse(
    @JsonProperty("msg")
    val message: String
) : CommandResponsePayload