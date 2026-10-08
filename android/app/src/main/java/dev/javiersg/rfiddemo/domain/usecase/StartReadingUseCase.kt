package dev.javiersg.rfiddemo.domain.usecase

import dev.javiersg.rfiddemo.domain.repository.RfidRepository

class StartReadingUseCase(
    private val repository: RfidRepository
) {
    suspend operator fun invoke() = repository.startReading()
}
