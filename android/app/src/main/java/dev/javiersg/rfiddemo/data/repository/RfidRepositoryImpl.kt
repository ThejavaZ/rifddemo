package dev.javiersg.rfiddemo.data.repository

import dev.javiersg.rfiddemo.data.database.mapper.DEFAULT_ANTENNA
import dev.javiersg.rfiddemo.domain.model.ReaderStatus
import dev.javiersg.rfiddemo.domain.model.RfidTag
import dev.javiersg.rfiddemo.domain.repository.LocalTagRepository
import dev.javiersg.rfiddemo.domain.repository.RfidReader
import dev.javiersg.rfiddemo.domain.repository.RfidRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class RfidRepositoryImpl(
    private val reader: RfidReader,
    private val localTagRepository: LocalTagRepository,
) : RfidRepository {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    init {
        // Offline-first: cada lectura se persiste en Room como PENDING sin bloquear la UI,
        // que colecta el mismo flujo de forma independiente.
        scope.launch {
            reader.tagFlow.collect { tag ->
                localTagRepository.processScannedTag(
                    epc = tag.epc,
                    rssi = tag.rssi,
                    antenna = DEFAULT_ANTENNA,
                )
            }
        }
    }

    override val readerStatus: StateFlow<ReaderStatus> = reader.readerStatus

    override val tags: Flow<RfidTag> = reader.tagFlow

    override suspend fun connect() = reader.connect()

    override suspend fun disconnect() = reader.disconnect()

    override suspend fun startReading() = reader.startScanning()

    override suspend fun stopReading() = reader.stopScanning()
}
