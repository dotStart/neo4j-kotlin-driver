package tv.dotstart.neo4j.kotlin.testkit.message.response

import com.fasterxml.jackson.annotation.JsonTypeName

@JsonTypeName("Record")
data class RecordResponse(
    val values: Map<String, Any?>
) : CommandResponsePayload