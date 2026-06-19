package com.screenlink.tv.domain.commands.repositories

import com.screenlink.tv.domain.commands.models.ScreenCommand
import com.screenlink.tv.domain.device.models.DeviceCredentials
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

enum class ConnectionState {
    DISCONNECTED,
    CONNECTING,
    CONNECTED,
    RECONNECTING,
    UNAUTHORIZED,
    ERROR,
}

interface CommandRepository {
    val connectionState: StateFlow<ConnectionState>
    val commands: Flow<ScreenCommand>

    fun connect(credentials: DeviceCredentials)

    fun disconnect()

    fun acknowledge(commandId: String)

    fun reportError(commandId: String, message: String)
}
