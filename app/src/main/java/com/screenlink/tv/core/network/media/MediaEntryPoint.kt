package com.screenlink.tv.core.network.media

import com.screenlink.tv.core.config.AppConfig
import com.screenlink.tv.core.logging.SafeLogger
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient

@EntryPoint
@InstallIn(SingletonComponent::class)
interface MediaEntryPoint {
    fun appConfig(): AppConfig

    fun logger(): SafeLogger

    @MediaHttpClient
    fun mediaHttpClient(): OkHttpClient
}
