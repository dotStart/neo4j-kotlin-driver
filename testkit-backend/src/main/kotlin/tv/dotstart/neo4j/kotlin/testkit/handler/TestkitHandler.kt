package tv.dotstart.neo4j.kotlin.testkit.handler

import com.neo4j.driver.kotlin.Driver
import com.neo4j.driver.kotlin.network.protocol.BoltProtocol
import io.netty.channel.ChannelHandlerContext
import org.apache.logging.log4j.LogManager
import tv.dotstart.neo4j.kotlin.testkit.handler.annotation.CommandHandler
import tv.dotstart.neo4j.kotlin.testkit.message.auth.CommonAuthorizationParameters
import tv.dotstart.neo4j.kotlin.testkit.message.command.NewDriverCommand
import tv.dotstart.neo4j.kotlin.testkit.message.command.NewSessionCommand
import tv.dotstart.neo4j.kotlin.testkit.message.command.SessionCloseCommand
import tv.dotstart.neo4j.kotlin.testkit.message.command.SessionRunCommand
import tv.dotstart.neo4j.kotlin.testkit.message.response.*
import java.io.PrintWriter
import java.io.StringWriter
import java.net.URI

class TestkitHandler : AbstractMappedChannelInboundHandler() {

    private val logger = LogManager.getLogger("testkit")

    private var driverErrorIdPool = 0

    private val driverInstances = mutableMapOf<Int, Driver>()
    private val connectionInstances = mutableMapOf<Int, BoltProtocol>()
    private val results = mutableMapOf<Int, List<Map<String, Any?>>>()

    override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
        if (cause is OperationAbortedException) {
            logger.debug("Aborted operation due to driver error", cause)
            return
        }

        super.exceptionCaught(ctx, cause)

        if (!ctx.channel().isOpen) {
            return
        }

        val msg = StringWriter()
            .also {
                PrintWriter(it).use(cause::printStackTrace)
            }
            .toString()

        ctx.writeAndFlush(CommandResponse(BackendErrorResponse(msg)))
    }

    fun <R> invokeDriver(ctx: ChannelHandlerContext, block: () -> R): R {
        return try {
            block()
        } catch (ex: Throwable) {
            val nextId = this.driverErrorIdPool++

            val msg = StringWriter()
                .also {
                    PrintWriter(it).use(ex::printStackTrace)
                }
                .toString()

            logger.error("Caught driver error #$nextId", ex)
            ctx.writeAndFlush(
                CommandResponse(
                    DriverErrorResponse(
                        nextId.toString(),
                        ex::class.simpleName ?: "<Unknown>",
                        msg
                    )
                )
            )

            throw OperationAbortedException(ex)
        }
    }

    @CommandHandler
    fun handleNewDriver(ctx: ChannelHandlerContext, msg: NewDriverCommand) {
        val authorizationParameters = msg.authorizationToken.parameters

        require(authorizationParameters is CommonAuthorizationParameters) { "Unsupported authorization parameters" }
        require(authorizationParameters.realm.isBlank() && authorizationParameters.ticket.isBlank()) {
            "Unsupported authorization parameters: Cannot authorize via kerberos"
        }

        val username = authorizationParameters.principal
            .takeIf { it.isNotEmpty() }
        val credentials = authorizationParameters.credentials
            .takeIf { it.isNotEmpty() }

        val nextId = this.driverInstances.size
        val driver = this.invokeDriver(ctx) { Driver(URI.create(msg.uri), username, credentials) }

        logger.info("Allocated driver $nextId for connection URI ${msg.uri}")
        this.driverInstances[nextId] = driver

        ctx.writeAndFlush(CommandResponse(DriverCommandResponse(nextId.toString())))
    }

    @CommandHandler
    fun handleNewSession(ctx: ChannelHandlerContext, msg: NewSessionCommand) {
        val driverId = msg.driverId
            .toInt(10)

        val driver = this.driverInstances[driverId]
            ?: throw IllegalArgumentException("Illegal driver instance: $driverId")

        val nextId = this.connectionInstances.size
        val connection = this.invokeDriver(ctx) { driver.connect() }

        logger.info("Allocated connection $nextId for driver $driverId")
        this.connectionInstances[nextId] = connection

        ctx.writeAndFlush(CommandResponse(SessionCommandResponse(nextId.toString())))
    }

    @CommandHandler
    fun handleSessionRun(ctx: ChannelHandlerContext, msg: SessionRunCommand) {
        val connectionId = msg.sessionId
            .toInt(10)

        val connection = this.connectionInstances[connectionId]
            ?: throw IllegalArgumentException("Illegal connection instance: $connectionId")

        val nextId = this.results.size
        val result = this.invokeDriver(ctx) { connection.run(msg.cypher, msg.parameters ?: emptyMap()) }

        logger.info("Allocated result $nextId for connection $connectionId")
        this.results[nextId] = result

        ctx.writeAndFlush(CommandResponse(SessionRunCommandResponse(nextId.toString())))
    }

    @CommandHandler
    fun handleSessionClose(ctx: ChannelHandlerContext, msg: SessionCloseCommand) {
        val connectionId = msg.sessionId
            .toInt(10)

        val connection = this.connectionInstances.remove(connectionId)
            ?: throw IllegalArgumentException("Illegal connection instance: $connectionId")

        this.invokeDriver(ctx) { connection.close() }
        logger.info("Closed connection $connectionId")

        ctx.writeAndFlush(CommandResponse(SessionCommandResponse(connectionId.toString())))
    }

    override fun channelInactive(ctx: ChannelHandlerContext) {
        logger.info("Frontend disconnected - Freeing remaining resources")

        this.connectionInstances.forEach { (_, connection) ->
            try {
                connection.close()
            } catch (ex: Throwable) {
                logger.warn("Failed to close connection", ex)
            }
        }
        this.driverInstances.forEach { (_, driver) ->
            try {
                driver.close()
            } catch (ex: Throwable) {
                logger.warn("Failed to close driver", ex)
            }
        }
    }

    /**
     * Special exception type for the purposes of notifying the pipeline about aborted operations due to a driver error.
     */
    private class OperationAbortedException(cause: Throwable) : Exception(null, cause)
}