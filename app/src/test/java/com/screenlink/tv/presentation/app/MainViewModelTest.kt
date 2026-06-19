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
        override val connectionState: StateFlow<ConnectionState> = mutableConnectionState
        override val commands: Flow<ScreenCommand> = mutableCommands
        override fun connect(credentials: DeviceCredentials) {
            mutableConnectionState.value = ConnectionState.CONNECTING
        }
        override fun disconnect() {
            mutableConnectionState.value = ConnectionState.DISCONNECTED
        }
        override fun acknowledge(commandId: String) = Unit
        override fun reportError(commandId: String, message: String) = Unit
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
