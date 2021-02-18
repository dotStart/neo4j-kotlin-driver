package tv.dotstart.neo4j.kotlin.testkit.message.command

import com.fasterxml.jackson.annotation.JsonTypeName

@JsonTypeName("SessionClose")
data class SessionCloseCommand(
    val sessionId: String
) : CommandParameters