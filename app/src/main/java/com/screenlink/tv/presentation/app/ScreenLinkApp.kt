package com.screenlink.tv.presentation.app

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.screenlink.tv.BuildConfig
import com.screenlink.tv.common.theme.ScreenLinkTheme
import com.screenlink.tv.domain.commands.repositories.ConnectionState
import com.screenlink.tv.playback.image.FullscreenImage
import com.screenlink.tv.playback.playlist.PlaylistRenderer
import com.screenlink.tv.playback.video.FullscreenVideo
import com.screenlink.tv.presentation.idle.screens.IdleScreen
import com.screenlink.tv.presentation.pairing.screens.PairingScreen
import com.screenlink.tv.presentation.status.components.ConnectingScreen
import com.screenlink.tv.presentation.status.components.ErrorScreen

@Composable
fun ScreenLinkApp(viewModel: MainViewModel) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    ScreenLinkTheme {
        when (val mode = state.mode) {
            ReceiverMode.Starting -> ConnectingScreen("Starting ScreenLink...")
            is ReceiverMode.Pairing -> PairingScreen(
                code = mode.code,
                expiresAt = mode.expiresAt,
                message = mode.message,
                onRefresh = viewModel::refreshPairing,
                onClearDebug = if (BuildConfig.DEBUG) viewModel::clearCredentialsForDebug else null,
            )
            is ReceiverMode.Connecting -> ConnectingScreen(mode.message)
            ReceiverMode.Idle -> IdleScreen(
                appVersion = BuildConfig.APP_VERSION_NAME,
                connectionLabel = state.connection.toDisplayLabel(),
            )
            ReceiverMode.Blank -> IdleScreen(showBranding = false)
            is ReceiverMode.Image -> FullscreenImage(
                url = mode.url,
                onReady = { viewModel.playbackReady(mode.commandId) },
                onError = { viewModel.playbackFailed(mode.commandId, it) },
            )
            is ReceiverMode.Video -> FullscreenVideo(
                url = mode.url,
                onReady = { viewModel.playbackReady(mode.commandId) },
                onError = { viewModel.playbackFailed(mode.commandId, it) },
            )
            is ReceiverMode.Playlist -> PlaylistRenderer(
                items = mode.items,
                loop = mode.loop,
                onReady = { viewModel.playbackReady(mode.commandId) },
                onError = { viewModel.playbackFailed(mode.commandId, it) },
            )
            is ReceiverMode.Error -> ErrorScreen(
                title = mode.title,
                message = mode.message,
                onRetry = if (mode.retryPairing) viewModel::refreshPairing else null,
            )
        }
    }
}

private fun ConnectionState.toDisplayLabel(): String = when (this) {
    ConnectionState.CONNECTED -> "online"
    ConnectionState.CONNECTING -> "connecting"
    ConnectionState.RECONNECTING -> "reconnecting"
    ConnectionState.ERROR -> "offline"
    ConnectionState.UNAUTHORIZED -> "unauthorized"
    ConnectionState.DISCONNECTED -> "offline"
}
