package tv.dotstart.neo4j.kotlin.testkit.message.response

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonTypeName

@JsonTypeName("DriverError")
data class DriverErrorResponse(
    val id: String,
    val errorType: String,
    @JsonProperty("msg")
    val message: String
) : CommandResponsePayload