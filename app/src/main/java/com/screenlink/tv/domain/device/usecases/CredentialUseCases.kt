package com.screenlink.tv.domain.device.usecases

import com.screenlink.tv.domain.device.models.DeviceCredentials
import com.screenlink.tv.domain.device.repositories.CredentialsRepository
import javax.inject.Inject

class GetCredentialsUseCase @Inject constructor(private val repository: CredentialsRepository) {
    suspend operator fun invoke() = repository.get()
}

class SaveCredentialsUseCase @Inject constructor(private val repository: CredentialsRepository) {
    suspend operator fun invoke(credentials: DeviceCredentials) = repository.save(credentials)
}

class ClearCredentialsUseCase @Inject constructor(private val repository: CredentialsRepository) {
    suspend operator fun invoke() = repository.clear()
}
