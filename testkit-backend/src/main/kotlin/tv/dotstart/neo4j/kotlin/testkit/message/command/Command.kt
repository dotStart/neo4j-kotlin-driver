package tv.dotstart.neo4j.kotlin.testkit.message.command

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonTypeInfo

data class Command(
    val name: String,
    @JsonProperty("data")
    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXTERNAL_PROPERTY, property = "name")
    val parameters: CommandParameters
)