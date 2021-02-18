package tv.dotstart.neo4j.kotlin.testkit.message.command

import com.fasterxml.jackson.annotation.JsonTypeName

@JsonTypeName("DriverClose")
data class DriverCloseCommand(
    val driverId: String
) : CommandParameters