package com.screenlink.tv.core.network.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PairingRequestDto(
    val deviceName: String,
    val deviceModel: String? = null,
    val appVersion: String,
)

@Serializable
data class PairingResponseDto(
    val screenId: String,
    @SerialName("pairingCode") val pairingCode: String,
    @SerialName("expiresAt") val expiresAt: String,
)

@Serializable
data class PairingStatusDto(
    val status: String,
    val deviceToken: String? = null,
)
