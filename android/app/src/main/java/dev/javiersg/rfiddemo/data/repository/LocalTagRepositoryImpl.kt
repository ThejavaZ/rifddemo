package dev.javiersg.rfiddemo.data.repository

import dev.javiersg.rfiddemo.data.local.dao.TagDao
import dev.javiersg.rfiddemo.data.local.entity.TagEntity
import dev.javiersg.rfiddemo.data.sync.SyncScheduler
import dev.javiersg.rfiddemo.domain.hardware.RfidReaderService
import dev.javiersg.rfiddemo.domain.repository.LocalTagRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap

class LocalTagRepositoryImpl(
    private val rfidTagDao: TagDao,
    private val rfidReaderService: RfidReaderService,
    private val syncScheduler: SyncScheduler
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

    override fun getTags(): Flow<List<TagEntity>> {
        return rfidTagDao.getAllTags()
    }

    override suspend fun processScannedTag(epc: String, rssi: Int, antenna: Int) {
        if (scannedEpcsCache.add(epc)) {
            val tag = TagEntity(
                epc = epc,
                rssi = rssi,
                antenna = antenna,
                readCount = 1
            )
            rfidTagDao.upsertTag(tag)
            syncScheduler.scheduleSync()
        }
    }

    override suspend fun getPendingSyncTags(): List<TagEntity> {
        return rfidTagDao.getUnsyncedTags()
    }

    override suspend fun markAsSyncing(epcs: List<String>) {
        rfidTagDao.markAsSyncing(epcs, timestamp = System.currentTimeMillis())
    }

    override suspend fun markAsSynced(epcs: List<String>) {
        rfidTagDao.markAsSynced(epcs, timestamp = System.currentTimeMillis())
    }

    override suspend fun markAsFailed(epcs: List<String>) {
        rfidTagDao.markAsFailed(epcs, timestamp = System.currentTimeMillis())
    }

    override suspend fun clearTags() {
        scannedEpcsCache.clear()
        rfidTagDao.clearAll()
    }
}