package com.screenlink.tv.presentation.app

import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.screenlink.tv.BuildConfig
import com.screenlink.tv.core.config.AppConfig
import com.screenlink.tv.core.logging.SafeLogger
import com.screenlink.tv.core.result.AppResult
import com.screenlink.tv.domain.commands.models.ScreenCommand
import com.screenlink.tv.domain.commands.repositories.CommandRepository
import com.screenlink.tv.domain.commands.repositories.ConnectionState
import com.screenlink.tv.domain.device.models.DeviceCredentials
import com.screenlink.tv.domain.device.usecases.ClearCredentialsUseCase
import com.screenlink.tv.domain.device.usecases.GetCredentialsUseCase
import com.screenlink.tv.domain.device.usecases.SaveCredentialsUseCase
import com.screenlink.tv.domain.pairing.models.PairingRequest
import com.screenlink.tv.domain.pairing.models.PairingStatus
import com.screenlink.tv.domain.pairing.usecases.GetPairingStatusUseCase
import com.screenlink.tv.domain.pairing.usecases.RequestPairingUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val getCredentials: GetCredentialsUseCase,
    private val saveCredentials: SaveCredentialsUseCase,
    private val clearCredentials: ClearCredentialsUseCase,
    private val requestPairing: RequestPairingUseCase,
    private val getPairingStatus: GetPairingStatusUseCase,
    private val commandRepository: CommandRepository,
    private val appConfig: AppConfig,
    private val logger: SafeLogger,
) : ViewModel() {
    private val mutableUiState = MutableStateFlow(ReceiverUiState())
    val uiState = mutableUiState.asStateFlow()

    private var pairingJob: Job? = null
    private val completedCommands = mutableSetOf<String>()

    init {
        observeRealtime()
        viewModelScope.launch {
            getCredentials()?.let(::connect) ?: refreshPairing()
        }
    }

    fun refreshPairing() {
        pairingJob?.cancel()
        commandRepository.disconnect()
        pairingJob = viewModelScope.launch {
            mutableUiState.update { it.copy(mode = ReceiverMode.Starting) }
            val request = PairingRequest(
                deviceName = Build.MODEL.ifBlank { "ScreenLink TV" },
                deviceModel = Build.DEVICE,
                appVersion = appConfig.appVersion,
            )
            when (val result = requestPairing(request)) {
                is AppResult.Failure -> showPairingError(result.message)
                is AppResult.Success -> {
                    val pairing = result.value
                    mutableUiState.update {
                        it.copy(mode = ReceiverMode.Pairing(pairing.code, pairing.expiresAt))
                    }
                    pollPairing(pairing.screenId)
                }
            }
        }
    }

    fun clearCredentialsForDebug() {
        if (!BuildConfig.DEBUG) return
        viewModelScope.launch {
            clearCredentials()
            refreshPairing()
        }
    }

    fun playbackReady(commandId: String) = completeCommand(commandId)

    fun playbackFailed(commandId: String, message: String) {
        commandRepository.reportError(commandId, message)
        logger.error("Playback failed")
        mutableUiState.update { it.copy(mode = ReceiverMode.Error("Playback error", message)) }
    }

    private suspend fun pollPairing(screenId: String) {
        while (pairingJob?.isActive == true) {
            delay(POLL_INTERVAL_MS)
            when (val result = getPairingStatus(screenId)) {
                is AppResult.Failure -> {
                    logger.error("Pairing status polling failed", result.cause)
                    if (result.message == PAIRING_SESSION_UNAVAILABLE) {
                        showPairingError(result.message)
                        return
                    }
                    updatePairingMessage("${result.message}. Retrying...")
                }
                is AppResult.Success -> when (val status = result.value) {
                    PairingStatus.Pending -> {
                        logger.info("Pairing pending")
                        updatePairingMessage(null)
                    }
                    is PairingStatus.Paired -> {
                        val token = status.deviceToken
                        if (token.isNullOrBlank()) {
                            showPairingError("This pairing token was already consumed. Refresh to request a new code.")
                        } else {
                            val credentials = DeviceCredentials(screenId, token)
                            saveCredentials(credentials)
                            logger.info("Pairing completed")
                            connect(credentials)
                        }
                        return
                    }
                    is PairingStatus.Unknown -> updatePairingMessage("Unexpected pairing status. Retrying...")
                }
            }
        }
    }

    private fun connect(credentials: DeviceCredentials) {
        pairingJob?.cancel()
        mutableUiState.update { it.copy(mode = ReceiverMode.Connecting()) }
        commandRepository.connect(credentials)
    }

    private fun observeRealtime() {
        viewModelScope.launch {
            commandRepository.connectionState.collect { state ->
                mutableUiState.update { current -> current.copy(connection = state) }
                when (state) {
                    ConnectionState.CONNECTED -> if (mutableUiState.value.mode is ReceiverMode.Connecting) {
                        mutableUiState.update { it.copy(mode = ReceiverMode.Idle) }
                    }
                    ConnectionState.UNAUTHORIZED -> {
                        clearCredentials()
                        refreshPairing()
                    }
                    ConnectionState.ERROR, ConnectionState.RECONNECTING -> {
                        if (mutableUiState.value.mode is ReceiverMode.Connecting) {
                            mutableUiState.update { it.copy(mode = ReceiverMode.Connecting("Offline. Reconnecting...")) }
                        }
                    }
                    else -> Unit
                }
            }
        }
        viewModelScope.launch {
            commandRepository.commands.collect(::handleCommand)
        }
    }

    private fun handleCommand(command: ScreenCommand) {
        completedCommands.remove(command.id)
        when (command) {
            is ScreenCommand.DisplayImage -> mutableUiState.update {
                it.copy(mode = ReceiverMode.Image(command.id, command.url))
            }
            is ScreenCommand.DisplayVideo -> mutableUiState.update {
                it.copy(mode = ReceiverMode.Video(command.id, command.url))
            }
            is ScreenCommand.PlayPlaylist -> mutableUiState.update {
                it.copy(mode = ReceiverMode.Playlist(command.id, command.items, command.loop))
            }
            is ScreenCommand.ClearScreen -> {
                mutableUiState.update { it.copy(mode = ReceiverMode.Blank) }
                completeCommand(command.id)
            }
            is ScreenCommand.Ping, is ScreenCommand.SyncContent -> completeCommand(command.id)
            is ScreenCommand.Unsupported -> commandRepository.reportError(command.id, "Unsupported command: ${command.type}")
            is ScreenCommand.Invalid -> commandRepository.reportError(command.id, command.reason)
        }
    }

    private fun completeCommand(commandId: String) {
        if (completedCommands.add(commandId)) commandRepository.acknowledge(commandId)
    }

    private fun updatePairingMessage(message: String?) {
        val current = mutableUiState.value.mode as? ReceiverMode.Pairing ?: return
        mutableUiState.update { it.copy(mode = current.copy(message = message)) }
    }

    private fun showPairingError(message: String) {
        mutableUiState.update {
            it.copy(mode = ReceiverMode.Error("Pairing unavailable", message, retryPairing = true))
        }
    }

    override fun onCleared() {
        commandRepository.disconnect()
        super.onCleared()
    }

    private companion object {
        const val PAIRING_SESSION_UNAVAILABLE = "Pairing session expired or unavailable. Refresh the pairing code."
        const val POLL_INTERVAL_MS = 2_500L
    }
}
