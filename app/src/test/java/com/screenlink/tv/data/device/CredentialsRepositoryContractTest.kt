package com.screenlink.tv.data.device

import com.screenlink.tv.domain.device.models.DeviceCredentials
import com.screenlink.tv.domain.device.repositories.CredentialsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class CredentialsRepositoryContractTest {
    @Test
    fun `credentials can be saved read and cleared`() = runTest {
        val repository: CredentialsRepository = FakeCredentialsRepository()
        val expected = DeviceCredentials("screen-1", "token")
        repository.save(expected)
        assertEquals(expected, repository.get())
        repository.clear()
        assertNull(repository.get())
    }

    private class FakeCredentialsRepository : CredentialsRepository {
        private val state = MutableStateFlow<DeviceCredentials?>(null)
        override val credentials: Flow<DeviceCredentials?> = state

        override suspend fun get() = state.value

        override suspend fun save(credentials: DeviceCredentials) {
            state.value = credentials
        }

        override suspend fun clear() {
            state.value = null
        }
    }
}
