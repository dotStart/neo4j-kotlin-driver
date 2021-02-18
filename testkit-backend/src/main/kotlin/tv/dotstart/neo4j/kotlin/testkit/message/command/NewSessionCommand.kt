package tv.dotstart.neo4j.kotlin.testkit.message.command

import com.fasterxml.jackson.annotation.JsonTypeName

@JsonTypeName("NewSession")
data class NewSessionCommand(
    val driverId: String,
    val accessMode: String,
    val bookmarks: List<String>?,
    val database: String?,
    val fetchSize: Long? = null
) : CommandParameters