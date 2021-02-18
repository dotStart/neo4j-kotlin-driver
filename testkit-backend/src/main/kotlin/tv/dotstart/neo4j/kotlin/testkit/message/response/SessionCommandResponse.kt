package tv.dotstart.neo4j.kotlin.testkit.message.response

import com.fasterxml.jackson.annotation.JsonTypeName

@JsonTypeName("Session")
data class SessionCommandResponse(
    val id: String
) : CommandResponsePayload