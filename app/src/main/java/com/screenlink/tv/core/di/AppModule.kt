package com.screenlink.tv.core.di

import com.screenlink.tv.core.logging.AndroidSafeLogger
import com.screenlink.tv.core.logging.SafeLogger
import com.screenlink.tv.core.network.websocket.SocketIoDeviceClient
import com.screenlink.tv.core.storage.CredentialsDataStore
import com.screenlink.tv.data.pairing.repositories.PairingRepositoryImpl
import com.screenlink.tv.domain.commands.repositories.CommandRepository
import com.screenlink.tv.domain.device.repositories.CredentialsRepository
import com.screenlink.tv.domain.pairing.repositories.PairingRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AppBindingsModule {
    @Binds abstract fun bindLogger(implementation: AndroidSafeLogger): SafeLogger

    @Binds abstract fun bindCredentials(implementation: CredentialsDataStore): CredentialsRepository

    @Binds abstract fun bindPairing(implementation: PairingRepositoryImpl): PairingRepository

    @Binds abstract fun bindCommands(implementation: SocketIoDeviceClient): CommandRepository
}

@Module
@InstallIn(SingletonComponent::class)
object AppProvidesModule {
    @Provides
    @Singleton
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
    }

    @Provides
    @Singleton
    fun provideHttpClient(): OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .callTimeout(20, TimeUnit.SECONDS)
        .build()
}
