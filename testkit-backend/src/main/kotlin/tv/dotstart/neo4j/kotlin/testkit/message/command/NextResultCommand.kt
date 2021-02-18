package tv.dotstart.neo4j.kotlin.testkit.message.command

import com.fasterxml.jackson.annotation.JsonTypeName

@JsonTypeName("ResultNext")
data class NextResultCommand(
    val resultId: String
) : CommandParameters