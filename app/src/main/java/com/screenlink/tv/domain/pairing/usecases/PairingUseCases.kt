package com.screenlink.tv.domain.pairing.usecases

import com.screenlink.tv.domain.pairing.models.PairingRequest
import com.screenlink.tv.domain.pairing.repositories.PairingRepository
import javax.inject.Inject

class RequestPairingUseCase @Inject constructor(private val repository: PairingRepository) {
    suspend operator fun invoke(request: PairingRequest) = repository.requestPairing(request)
}

class GetPairingStatusUseCase @Inject constructor(private val repository: PairingRepository) {
    suspend operator fun invoke(screenId: String) = repository.getStatus(screenId)
}
