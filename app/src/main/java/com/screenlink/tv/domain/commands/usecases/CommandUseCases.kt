package com.screenlink.tv.domain.commands.usecases

import com.screenlink.tv.domain.commands.repositories.CommandRepository
import com.screenlink.tv.domain.device.models.DeviceCredentials
import javax.inject.Inject

class ConnectDeviceUseCase @Inject constructor(private val repository: CommandRepository) {
    operator fun invoke(credentials: DeviceCredentials) = repository.connect(credentials)
}

class AcknowledgeCommandUseCase @Inject constructor(private val repository: CommandRepository) {
    operator fun invoke(commandId: String) = repository.acknowledge(commandId)
}

class ReportCommandErrorUseCase @Inject constructor(private val repository: CommandRepository) {
    operator fun invoke(commandId: String, message: String) = repository.reportError(commandId, message)
}
