package com.screenlink.tv.domain.device.repositories

import com.screenlink.tv.domain.device.models.DeviceCredentials
import kotlinx.coroutines.flow.Flow

interface CredentialsRepository {
    val credentials: Flow<DeviceCredentials?>

    suspend fun get(): DeviceCredentials?

    suspend fun save(credentials: DeviceCredentials)

    suspend fun clear()
}
