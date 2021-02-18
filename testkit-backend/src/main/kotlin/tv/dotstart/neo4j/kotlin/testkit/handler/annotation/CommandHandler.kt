package tv.dotstart.neo4j.kotlin.testkit.handler.annotation

@MustBeDocumented
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class CommandHandler
