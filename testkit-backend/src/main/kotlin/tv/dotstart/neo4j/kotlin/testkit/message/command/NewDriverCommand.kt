package tv.dotstart.neo4j.kotlin.testkit.message.command

import com.fasterxml.jackson.annotation.JsonTypeName
import tv.dotstart.neo4j.kotlin.testkit.message.auth.AuthorizationToken

@JsonTypeName("NewDriver")
data class NewDriverCommand(
    val uri: String,
    val authorizationToken: AuthorizationToken,
    val userAgent: String?
) : CommandParameters {
}