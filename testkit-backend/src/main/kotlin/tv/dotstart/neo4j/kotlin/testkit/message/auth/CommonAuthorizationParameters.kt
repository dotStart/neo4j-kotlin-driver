package tv.dotstart.neo4j.kotlin.testkit.message.auth

import com.fasterxml.jackson.annotation.JsonTypeName

@JsonTypeName("AuthorizationToken")
data class CommonAuthorizationParameters(
    val scheme: String,
    val principal: String,
    val credentials: String,
    val realm: String,
    val ticket: String
) : AuthorizationParameters