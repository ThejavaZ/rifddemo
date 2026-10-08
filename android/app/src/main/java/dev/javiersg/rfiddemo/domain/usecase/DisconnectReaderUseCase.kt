package dev.javiersg.rfiddemo.domain.usecase

import dev.javiersg.rfiddemo.domain.repository.RfidRepository

class DisconnectReaderUseCase(
    private val repository: RfidRepository
) {
    suspend operator fun invoke() = repository.disconnect()
}
