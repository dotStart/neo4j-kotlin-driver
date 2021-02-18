package tv.dotstart.neo4j.kotlin.testkit.message.command

import com.fasterxml.jackson.annotation.JsonSubTypes

@JsonSubTypes(
    JsonSubTypes.Type(name = "NewDriver", value = NewDriverCommand::class),
    JsonSubTypes.Type(name = "NewSession", value = NewSessionCommand::class),
    JsonSubTypes.Type(name = "SessionRun", value = SessionRunCommand::class),
    JsonSubTypes.Type(name = "SessionClose", value = SessionCloseCommand::class)
)
interface CommandParameters