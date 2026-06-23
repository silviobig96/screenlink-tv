package com.screenlink.tv.presentation.app

import com.screenlink.tv.core.config.AppConfig
import com.screenlink.tv.core.logging.SafeLogger
import com.screenlink.tv.core.result.AppResult
import com.screenlink.tv.domain.commands.models.ScreenCommand
import com.screenlink.tv.domain.commands.repositories.CommandRepository
import com.screenlink.tv.domain.commands.repositories.ConnectionState
import com.screenlink.tv.domain.device.models.DeviceCredentials
import com.screenlink.tv.domain.device.repositories.CredentialsRepository
import com.screenlink.tv.domain.device.usecases.ClearCredentialsUseCase
import com.screenlink.tv.domain.device.usecases.GetCredentialsUseCase
import com.screenlink.tv.domain.device.usecases.SaveCredentialsUseCase
import com.screenlink.tv.domain.pairing.models.PairingCode
import com.screenlink.tv.domain.pairing.models.PairingRequest
import com.screenlink.tv.domain.pairing.models.PairingStatus
import com.screenlink.tv.domain.pairing.repositories.PairingRepository
import com.screenlink.tv.domain.pairing.usecases.GetPairingStatusUseCase
import com.screenlink.tv.domain.pairing.usecases.RequestPairingUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModelTest {
    private val dispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `stored credentials connect then image command updates playback state`() = runTest(dispatcher) {
        val credentialsRepository = FakeCredentialsRepository(DeviceCredentials("screen-1", "token"))
        val commandRepository = FakeCommandRepository()
        val pairingRepository = FakePairingRepository()
        val viewModel = MainViewModel(
            getCredentials = GetCredentialsUseCase(credentialsRepository),
            saveCredentials = SaveCredentialsUseCase(credentialsRepository),
            clearCredentials = ClearCredentialsUseCase(credentialsRepository),
            requestPairing = RequestPairingUseCase(pairingRepository),
            getPairingStatus = GetPairingStatusUseCase(pairingRepository),
            commandRepository = commandRepository,
            appConfig = AppConfig(),
            logger = FakeLogger,
        )

        advanceUntilIdle()
        assertTrue(viewModel.uiState.value.mode is ReceiverMode.Connecting)

        commandRepository.mutableConnectionState.value = ConnectionState.CONNECTED
        advanceUntilIdle()
        assertEquals(ReceiverMode.Idle, viewModel.uiState.value.mode)

        commandRepository.mutableCommands.emit(ScreenCommand.DisplayImage("command-1", "https://example.com/image.jpg"))
        advanceUntilIdle()
        assertEquals("command-1", (viewModel.uiState.value.mode as ReceiverMode.Image).commandId)
    }

    @Test
    fun `clear screen switches to blank mode instead of branded idle`() = runTest(dispatcher) {
        val credentialsRepository = FakeCredentialsRepository(DeviceCredentials("screen-1", "token"))
        val commandRepository = FakeCommandRepository()
        val viewModel = createViewModel(credentialsRepository, commandRepository)

        advanceUntilIdle()
        commandRepository.mutableConnectionState.value = ConnectionState.CONNECTED
        advanceUntilIdle()

        commandRepository.mutableCommands.emit(ScreenCommand.ClearScreen("clear-1"))
        advanceUntilIdle()

        assertEquals(ReceiverMode.Blank, viewModel.uiState.value.mode)
        assertEquals("clear-1", commandRepository.acknowledgedCommandId)
    }

    @Test
    fun `image playback failure reports command error and shows error state`() = runTest(dispatcher) {
        val credentialsRepository = FakeCredentialsRepository(DeviceCredentials("screen-1", "token"))
        val commandRepository = FakeCommandRepository()
        val viewModel = createViewModel(credentialsRepository, commandRepository)

        advanceUntilIdle()
        viewModel.playbackFailed("command-1", "Image could not be loaded")

        assertEquals("command-1", commandRepository.failedCommandId)
        assertEquals("Image could not be loaded", commandRepository.failedMessage)
        val mode = viewModel.uiState.value.mode as ReceiverMode.Error
        assertEquals("Playback error", mode.title)
        assertEquals("Image could not be loaded", mode.message)
    }

    @Test
    fun `video playback failure reports command error and shows error state`() = runTest(dispatcher) {
        val credentialsRepository = FakeCredentialsRepository(DeviceCredentials("screen-1", "token"))
        val commandRepository = FakeCommandRepository()
        val viewModel = createViewModel(credentialsRepository, commandRepository)

        advanceUntilIdle()
        viewModel.playbackFailed("command-2", "Video could not be played")

        assertEquals("command-2", commandRepository.failedCommandId)
        assertEquals("Video could not be played", commandRepository.failedMessage)
        val mode = viewModel.uiState.value.mode as ReceiverMode.Error
        assertEquals("Playback error", mode.title)
        assertEquals("Video could not be played", mode.message)
    }

    private fun createViewModel(
        credentialsRepository: FakeCredentialsRepository,
        commandRepository: FakeCommandRepository,
        pairingRepository: FakePairingRepository = FakePairingRepository(),
    ) = MainViewModel(
        getCredentials = GetCredentialsUseCase(credentialsRepository),
        saveCredentials = SaveCredentialsUseCase(credentialsRepository),
        clearCredentials = ClearCredentialsUseCase(credentialsRepository),
        requestPairing = RequestPairingUseCase(pairingRepository),
        getPairingStatus = GetPairingStatusUseCase(pairingRepository),
        commandRepository = commandRepository,
        appConfig = AppConfig(),
        logger = FakeLogger,
    )

    private class FakeCredentialsRepository(initial: DeviceCredentials?) : CredentialsRepository {
        private val state = MutableStateFlow(initial)
        override val credentials: Flow<DeviceCredentials?> = state
        override suspend fun get() = state.value
        override suspend fun save(credentials: DeviceCredentials) {
            state.value = credentials
        }
        override suspend fun clear() {
            state.value = null
        }
    }

    private class FakeCommandRepository : CommandRepository {
        val mutableConnectionState = MutableStateFlow(ConnectionState.DISCONNECTED)
        val mutableCommands = MutableSharedFlow<ScreenCommand>(extraBufferCapacity = 1)
        var acknowledgedCommandId: String? = null
        var failedCommandId: String? = null
        var failedMessage: String? = null
        override val connectionState: StateFlow<ConnectionState> = mutableConnectionState
        override val commands: Flow<ScreenCommand> = mutableCommands
        override fun connect(credentials: DeviceCredentials) {
            mutableConnectionState.value = ConnectionState.CONNECTING
        }
        override fun disconnect() {
            mutableConnectionState.value = ConnectionState.DISCONNECTED
        }
        override fun acknowledge(commandId: String) {
            acknowledgedCommandId = commandId
        }
        override fun reportError(commandId: String, message: String) {
            failedCommandId = commandId
            failedMessage = message
        }
    }

    private class FakePairingRepository : PairingRepository {
        override suspend fun requestPairing(request: PairingRequest): AppResult<PairingCode> = AppResult.Failure("not expected")
        override suspend fun getStatus(screenId: String): AppResult<PairingStatus> = AppResult.Failure("not expected")
    }

    private data object FakeLogger : SafeLogger {
        override fun info(message: String) = Unit
        override fun error(message: String, throwable: Throwable?) = Unit
    }
}
