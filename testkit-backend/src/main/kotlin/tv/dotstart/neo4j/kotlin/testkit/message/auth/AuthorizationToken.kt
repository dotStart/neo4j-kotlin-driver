package tv.dotstart.neo4j.kotlin.testkit.message.auth

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonTypeInfo

data class AuthorizationToken(
    val name: String,
    @JsonProperty("data")
    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXTERNAL_PROPERTY, property = "name")
    val parameters: AuthorizationParameters
)