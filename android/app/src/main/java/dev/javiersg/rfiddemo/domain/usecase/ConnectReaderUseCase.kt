package dev.javiersg.rfiddemo.domain.usecase

import dev.javiersg.rfiddemo.domain.repository.RfidRepository
import javax.inject.Inject

class ConnectReaderUseCase
    @Inject
    constructor(
        private val repository: RfidRepository,
    ) {
        suspend operator fun invoke() = repository.connect()
    }
