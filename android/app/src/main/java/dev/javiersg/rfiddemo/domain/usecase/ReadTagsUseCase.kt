package dev.javiersg.rfiddemo.domain.usecase

import dev.javiersg.rfiddemo.domain.model.RfidTag
import dev.javiersg.rfiddemo.domain.repository.RfidRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ReadTagsUseCase
    @Inject
    constructor(
        private val repository: RfidRepository,
    ) {
        operator fun invoke(): Flow<RfidTag> = repository.tags
    }
