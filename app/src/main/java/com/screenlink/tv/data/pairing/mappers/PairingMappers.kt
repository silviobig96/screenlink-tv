package com.screenlink.tv.data.pairing.mappers

import com.screenlink.tv.core.network.dto.PairingResponseDto
import com.screenlink.tv.core.network.dto.PairingStatusDto
import com.screenlink.tv.domain.pairing.models.PairingCode
import com.screenlink.tv.domain.pairing.models.PairingStatus

fun PairingResponseDto.toDomain() = PairingCode(screenId, pairingCode, expiresAt)

fun PairingStatusDto.toDomain(): PairingStatus = when (status.lowercase()) {
    "pending" -> PairingStatus.Pending
    "paired" -> PairingStatus.Paired(deviceToken)
    else -> PairingStatus.Unknown(status)
}
