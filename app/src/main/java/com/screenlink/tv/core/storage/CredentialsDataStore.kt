package com.screenlink.tv.core.storage

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.screenlink.tv.domain.device.models.DeviceCredentials
import com.screenlink.tv.domain.device.repositories.CredentialsRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.credentialsDataStore by preferencesDataStore(name = "screenlink_credentials")

@Singleton
class CredentialsDataStore @Inject constructor(
    @ApplicationContext private val context: Context,
) : CredentialsRepository {
    override val credentials: Flow<DeviceCredentials?> = context.credentialsDataStore.data.map { preferences ->
        val screenId = preferences[SCREEN_ID]
        val token = preferences[DEVICE_TOKEN]
        if (screenId.isNullOrBlank() || token.isNullOrBlank()) null else DeviceCredentials(screenId, token)
    }

    override suspend fun get(): DeviceCredentials? = credentials.first()

    override suspend fun save(credentials: DeviceCredentials) {
        context.credentialsDataStore.edit {
            it[SCREEN_ID] = credentials.screenId
            it[DEVICE_TOKEN] = credentials.deviceToken
        }
    }

    override suspend fun clear() {
        context.credentialsDataStore.edit { it.clear() }
    }

    private companion object {
        val SCREEN_ID = stringPreferencesKey("screen_id")
        val DEVICE_TOKEN = stringPreferencesKey("device_token")
    }
}
