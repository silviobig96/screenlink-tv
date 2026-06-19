package com.screenlink.tv.data.pairing.repositories

import com.screenlink.tv.core.logging.SafeLogger
import com.screenlink.tv.core.network.api.PairingApi
import com.screenlink.tv.core.network.dto.PairingRequestDto
import com.screenlink.tv.core.result.AppResult
import com.screenlink.tv.data.pairing.mappers.toDomain
import com.screenlink.tv.domain.pairing.models.PairingCode
import com.screenlink.tv.domain.pairing.models.PairingRequest
import com.screenlink.tv.domain.pairing.models.PairingStatus
import com.screenlink.tv.domain.pairing.repositories.PairingRepository
import javax.inject.Inject

class PairingRepositoryImpl @Inject constructor(
    private val api: PairingApi,
    private val logger: SafeLogger,
) : PairingRepository {
    override suspend fun requestPairing(request: PairingRequest): AppResult<PairingCode> = runCatching {
        logger.info("Pairing requested")
        api.requestPairing(PairingRequestDto(request.deviceName, request.deviceModel, request.appVersion)).toDomain()
    }.fold({ AppResult.Success(it) }) {
        logger.error("Pairing request failed", it)
        AppResult.Failure("Unable to request a pairing code", it)
    }

    override suspend fun getStatus(screenId: String): AppResult<PairingStatus> = runCatching {
        api.getPairingStatus(screenId).toDomain()
    }.fold({ AppResult.Success(it) }) {
        AppResult.Failure("Unable to check pairing status", it)
    }
}
