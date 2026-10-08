package dev.javiersg.rfiddemo.data.repository

import dev.javiersg.rfiddemo.data.local.dao.RfidTagDao
import dev.javiersg.rfiddemo.data.local.entity.RfidTagEntity
import dev.javiersg.rfiddemo.domain.hardware.RfidReaderService
import dev.javiersg.rfiddemo.domain.repository.LocalTagRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap

class LocalTagRepositoryImpl(
    private val rfidTagDao: RfidTagDao,
    private val rfidReaderService: RfidReaderService
) : LocalTagRepository {

    private val scope = CoroutineScope(Dispatchers.IO)
    private val scannedEpcsCache = ConcurrentHashMap.newKeySet<String>()

    init {
        observeAndBatchScans()
    }

    private fun observeAndBatchScans() {
        scope.launch {
            rfidReaderService.scannedTags
                .buffer(capacity = 100)
                .collect { tagEvent ->
                    processScannedTag(
                        epc = tagEvent.epc,
                        rssi = tagEvent.rssi,
                        antenna = 1
                    )
                }
        }
    }

    override fun getTags(): Flow<List<RfidTagEntity>> {
        return rfidTagDao.getAllTags()
    }

    override suspend fun processScannedTag(epc: String, rssi: Int, antenna: Int) {
        if (scannedEpcsCache.add(epc)) {
            val tag = RfidTagEntity(
                epc = epc,
                rssi = rssi,
                antenna = antenna,
                readCount = 1
            )
            rfidTagDao.upsertTag(tag)
        }
    }

    override suspend fun getPendingSyncTags(): List<RfidTagEntity> {
        return emptyList()
    }

    override suspend fun markAsSynced(epcs: List<String>) {
        // Reservado para la sincronización remota
    }

    override suspend fun clearTags() {
        scannedEpcsCache.clear()
        rfidTagDao.clearAll()
    }
}