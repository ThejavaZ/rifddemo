package dev.javiersg.rfiddemo.data.repository

import dev.javiersg.rfiddemo.data.local.dao.TagDao
import dev.javiersg.rfiddemo.data.local.entity.TagEntity
import dev.javiersg.rfiddemo.data.sync.SyncScheduler
import dev.javiersg.rfiddemo.domain.repository.LocalTagRepository
import kotlinx.coroutines.flow.Flow
import java.util.concurrent.ConcurrentHashMap

/**
 * Persistencia local de tags. Las lecturas llegan desde [dev.javiersg.rfiddemo.data.repository.RfidRepositoryImpl]
 * (único puente lector -> Room) y cada inserción enlaza la cola de sincronización con WorkManager.
 */
class LocalTagRepositoryImpl(
    private val rfidTagDao: TagDao,
    private val syncScheduler: SyncScheduler,
) : LocalTagRepository {
    private val scannedEpcsCache = ConcurrentHashMap.newKeySet<String>()

    override fun getTags(): Flow<List<TagEntity>> = rfidTagDao.getAllTags()

    override suspend fun processScannedTag(
        epc: String,
        rssi: Int,
        antenna: Int,
    ) {
        if (scannedEpcsCache.add(epc)) {
            val tag =
                TagEntity(
                    epc = epc,
                    rssi = rssi,
                    antenna = antenna,
                    readCount = 1,
                )
            rfidTagDao.upsertTag(tag)
            syncScheduler.scheduleSync()
        }
    }

    override suspend fun getPendingSyncTags(): List<TagEntity> = rfidTagDao.getUnsyncedTags()

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
