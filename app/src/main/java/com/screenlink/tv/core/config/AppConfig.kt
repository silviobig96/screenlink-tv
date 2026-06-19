package com.screenlink.tv.core.config

import com.screenlink.tv.BuildConfig
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppConfig @Inject constructor() {
    val apiBaseUrl: String = BuildConfig.API_BASE_URL.trimEnd('/') + "/"
    val webSocketBaseUrl: String = BuildConfig.WS_BASE_URL.trimEnd('/')
    val appVersion: String = BuildConfig.APP_VERSION_NAME
}
