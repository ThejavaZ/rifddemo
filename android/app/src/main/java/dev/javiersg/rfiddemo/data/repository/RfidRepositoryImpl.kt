package dev.javiersg.rfiddemo.data.repository

import dev.javiersg.rfiddemo.domain.model.ReaderStatus
import dev.javiersg.rfiddemo.domain.model.RfidTag
import dev.javiersg.rfiddemo.domain.repository.IRfidReader
import dev.javiersg.rfiddemo.domain.repository.RfidRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

class RfidRepositoryImpl(
    private val reader: IRfidReader
) : RfidRepository {

    override val readerStatus: StateFlow<ReaderStatus> = reader.readerStatus

    override val tags: Flow<RfidTag> = reader.tagFlow

    override suspend fun connect() = reader.connect()

    override suspend fun disconnect() = reader.disconnect()

    override suspend fun startReading() = reader.startScanning()

    override suspend fun stopReading() = reader.stopScanning()
}
