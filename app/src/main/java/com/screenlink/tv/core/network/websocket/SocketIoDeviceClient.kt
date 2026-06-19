package com.screenlink.tv.core.network.websocket

import com.screenlink.tv.core.config.AppConfig
import com.screenlink.tv.core.logging.SafeLogger
import com.screenlink.tv.data.commands.mappers.CommandMapper
import com.screenlink.tv.domain.commands.models.ScreenCommand
import com.screenlink.tv.domain.commands.repositories.CommandRepository
import com.screenlink.tv.domain.commands.repositories.ConnectionState
import com.screenlink.tv.domain.device.models.DeviceCredentials
import io.socket.client.IO
import io.socket.client.Socket
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONObject
import java.net.URI
import java.util.Timer
import java.util.TimerTask
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SocketIoDeviceClient @Inject constructor(
    private val config: AppConfig,
    private val mapper: CommandMapper,
    private val logger: SafeLogger,
) : CommandRepository {
    private val mutableConnectionState = MutableStateFlow(ConnectionState.DISCONNECTED)
    override val connectionState = mutableConnectionState.asStateFlow()

    private val mutableCommands = MutableSharedFlow<ScreenCommand>(extraBufferCapacity = 16)
    override val commands: Flow<ScreenCommand> = mutableCommands.asSharedFlow()

    private var socket: Socket? = null
    private var heartbeatTimer: Timer? = null

    override fun connect(credentials: DeviceCredentials) {
        disconnect()
        mutableConnectionState.value = ConnectionState.CONNECTING
        val options = IO.Options.builder()
            .setAuth(
                mapOf(
                    "screenId" to credentials.screenId,
                    "deviceToken" to credentials.deviceToken,
                    "appVersion" to config.appVersion,
                ),
            )
            .setReconnection(true)
            .setReconnectionAttempts(Int.MAX_VALUE)
            .setReconnectionDelay(1_000)
            .setReconnectionDelayMax(15_000)
            .setRandomizationFactor(0.3)
            .setForceNew(true)
            .build()
        socket = IO.socket(URI.create(config.webSocketBaseUrl + "/devices"), options).also(::configureAndConnect)
    }

    override fun disconnect() {
        stopHeartbeat()
        socket?.off()
        socket?.disconnect()
        socket = null
        mutableConnectionState.value = ConnectionState.DISCONNECTED
    }

    override fun acknowledge(commandId: String) {
        socket?.emit(EVENT_ACK, JSONObject().put("commandId", commandId))
        logger.info("Command acknowledged")
    }

    override fun reportError(commandId: String, message: String) {
        socket?.emit(EVENT_ERROR, JSONObject().put("commandId", commandId).put("message", message.take(160)))
        logger.error("Command failed: ${message.take(80)}")
    }

    private fun configureAndConnect(deviceSocket: Socket) {
        deviceSocket.on(Socket.EVENT_CONNECT) {
            mutableConnectionState.value = ConnectionState.CONNECTED
            logger.info("Socket.IO connected")
            startHeartbeat()
        }
        deviceSocket.on(Socket.EVENT_DISCONNECT) {
            stopHeartbeat()
            mutableConnectionState.value = ConnectionState.RECONNECTING
            logger.info("Socket.IO disconnected")
        }
        deviceSocket.on(Socket.EVENT_CONNECT_ERROR) { args ->
            val message = args.firstOrNull()?.toString().orEmpty()
            val isUnauthorized =
                message.contains("unauthor", ignoreCase = true) ||
                    message.contains("token", ignoreCase = true)
            mutableConnectionState.value = if (isUnauthorized) {
                ConnectionState.UNAUTHORIZED
            } else {
                ConnectionState.ERROR
            }
            logger.error("Socket.IO connection failed")
        }
        deviceSocket.io().on("reconnect_attempt") {
            mutableConnectionState.value = ConnectionState.RECONNECTING
        }
        deviceSocket.on(EVENT_COMMAND) { args ->
            args.firstOrNull()?.let { raw ->
                logger.info("Command received")
                mutableCommands.tryEmit(mapper.map(raw))
            }
        }
        deviceSocket.connect()
    }

    private fun startHeartbeat() {
        stopHeartbeat()
        heartbeatTimer = Timer("screenlink-heartbeat", true).also { timer ->
            timer.scheduleAtFixedRate(
                object : TimerTask() {
                    override fun run() {
                        socket?.takeIf(Socket::connected)?.emit(EVENT_HEARTBEAT, JSONObject().put("timestamp", System.currentTimeMillis()))
                    }
                },
                0L,
                HEARTBEAT_INTERVAL_MS,
            )
        }
    }

    private fun stopHeartbeat() {
        heartbeatTimer?.cancel()
        heartbeatTimer = null
    }

    private companion object {
        const val EVENT_COMMAND = "screen.command"
        const val EVENT_HEARTBEAT = "screen.heartbeat"
        const val EVENT_ACK = "screen.command_ack"
        const val EVENT_ERROR = "screen.command_error"
        const val HEARTBEAT_INTERVAL_MS = 25_000L
    }
}
