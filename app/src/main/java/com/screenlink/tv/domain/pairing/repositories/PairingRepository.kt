package com.screenlink.tv.domain.pairing.repositories

import com.screenlink.tv.core.result.AppResult
import com.screenlink.tv.domain.pairing.models.PairingCode
import com.screenlink.tv.domain.pairing.models.PairingRequest
import com.screenlink.tv.domain.pairing.models.PairingStatus

interface PairingRepository {
    suspend fun requestPairing(request: PairingRequest): AppResult<PairingCode>

    suspend fun getStatus(screenId: String): AppResult<PairingStatus>
}
