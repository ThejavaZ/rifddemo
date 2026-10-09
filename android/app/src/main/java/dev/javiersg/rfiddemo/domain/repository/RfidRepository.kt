package dev.javiersg.rfiddemo.domain.repository

import dev.javiersg.rfiddemo.domain.model.ReaderStatus
import dev.javiersg.rfiddemo.domain.model.RfidTag
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface RfidRepository {
    val readerStatus: StateFlow<ReaderStatus>
    val tags: Flow<RfidTag>

    suspend fun connect()

    suspend fun disconnect()

    suspend fun startReading()

    suspend fun stopReading()
}
