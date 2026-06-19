package com.screenlink.tv.domain.pairing.models

data class PairingRequest(
    val deviceName: String,
    val deviceModel: String?,
    val appVersion: String,
)

data class PairingCode(
    val screenId: String,
    val code: String,
    val expiresAt: String,
)

sealed interface PairingStatus {
    data object Pending : PairingStatus

    data class Paired(val deviceToken: String?) : PairingStatus

    data class Unknown(val value: String) : PairingStatus
}
