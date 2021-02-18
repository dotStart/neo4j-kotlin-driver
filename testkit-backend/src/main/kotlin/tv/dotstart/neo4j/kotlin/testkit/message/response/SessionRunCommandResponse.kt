package tv.dotstart.neo4j.kotlin.testkit.message.response

import com.fasterxml.jackson.annotation.JsonTypeName

@JsonTypeName("Result")
data class SessionRunCommandResponse(
    val id: String
) : CommandResponsePayload