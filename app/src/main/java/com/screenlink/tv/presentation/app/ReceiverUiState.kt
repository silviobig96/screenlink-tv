package com.screenlink.tv.presentation.app

import com.screenlink.tv.domain.commands.models.PlaylistItem
import com.screenlink.tv.domain.commands.repositories.ConnectionState

data class ReceiverUiState(val mode: ReceiverMode = ReceiverMode.Starting, val connection: ConnectionState = ConnectionState.DISCONNECTED)

sealed interface ReceiverMode {
    data object Starting : ReceiverMode

    data class Pairing(val code: String, val expiresAt: String, val waiting: Boolean = true, val message: String? = null) : ReceiverMode

    data class Connecting(val message: String = "Connecting to ScreenLink...") : ReceiverMode

    data object Idle : ReceiverMode

    data object Blank : ReceiverMode

    data class Image(val commandId: String, val url: String) : ReceiverMode

    data class Video(val commandId: String, val url: String) : ReceiverMode

    data class Playlist(val commandId: String, val items: List<PlaylistItem>, val loop: Boolean) : ReceiverMode

    data class Error(val title: String, val message: String, val retryPairing: Boolean = false) : ReceiverMode
}
