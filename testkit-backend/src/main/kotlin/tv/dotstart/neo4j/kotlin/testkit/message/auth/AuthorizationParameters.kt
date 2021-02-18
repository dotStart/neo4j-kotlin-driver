package tv.dotstart.neo4j.kotlin.testkit.message.auth

import com.fasterxml.jackson.annotation.JsonSubTypes

@JsonSubTypes(
    JsonSubTypes.Type(name = "AuthorizationToken", value = CommonAuthorizationParameters::class)
)
interface AuthorizationParameters