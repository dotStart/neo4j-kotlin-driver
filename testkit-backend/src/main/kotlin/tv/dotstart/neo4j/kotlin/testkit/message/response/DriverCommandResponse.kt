package tv.dotstart.neo4j.kotlin.testkit.message.response

import com.fasterxml.jackson.annotation.JsonTypeName

@JsonTypeName("Driver")
data class DriverCommandResponse(
    val id: String
) : CommandResponsePayload