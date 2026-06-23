package com.screenlink.tv.data.pairing

import com.screenlink.tv.core.logging.SafeLogger
import com.screenlink.tv.core.network.api.HttpStatusException
import com.screenlink.tv.core.network.api.PairingApi
import com.screenlink.tv.core.network.dto.PairingStatusDto
import com.screenlink.tv.core.result.AppResult
import com.screenlink.tv.data.pairing.repositories.PairingRepositoryImpl
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException

class PairingRepositoryImplTest {
    private val api = mockk<PairingApi>()
    private val repository = PairingRepositoryImpl(api, FakeLogger)

    @Test
    fun `404 status maps to expired pairing message`() = runTest {
        coEvery { api.getPairingStatus("screen-1") } throws HttpStatusException(404, "HTTP 404")

        val result = repository.getStatus("screen-1")

        assertTrue(result is AppResult.Failure)
        assertEquals(
            "Pairing session expired or unavailable. Refresh the pairing code.",
            (result as AppResult.Failure).message,
        )
    }

    @Test
    fun `network status failures map to network unavailable`() = runTest {
        coEvery { api.getPairingStatus("screen-1") } throws IOException("timeout")

        val result = repository.getStatus("screen-1")

        assertTrue(result is AppResult.Failure)
        assertEquals("Network unavailable", (result as AppResult.Failure).message)
    }

    @Test
    fun `successful status response maps normally`() = runTest {
        coEvery { api.getPairingStatus("screen-1") } returns PairingStatusDto("paired", "token")

        val result = repository.getStatus("screen-1")

        assertTrue(result is AppResult.Success)
    }

    private data object FakeLogger : SafeLogger {
        override fun info(message: String) = Unit
        override fun error(message: String, throwable: Throwable?) = Unit
    }
}
