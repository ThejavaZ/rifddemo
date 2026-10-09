package dev.javiersg.rfiddemo.domain.usecase

import dev.javiersg.rfiddemo.domain.model.ReaderStatus
import dev.javiersg.rfiddemo.domain.repository.RfidRepository
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

class ObserveReaderStatusUseCase
    @Inject
    constructor(
        private val repository: RfidRepository,
    ) {
        operator fun invoke(): StateFlow<ReaderStatus> = repository.readerStatus
    }
