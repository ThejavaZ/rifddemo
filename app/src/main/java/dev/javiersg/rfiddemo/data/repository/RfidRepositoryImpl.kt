package dev.javiersg.rfiddemo.data.repository

import dev.javiersg.rfiddemo.data.local.SyncStatus
import dev.javiersg.rfiddemo.data.local.dao.RfidTagDao
import dev.javiersg.rfiddemo.data.local.entity.RfidTagEntity
import dev.javiersg.rfiddemo.domain.repository.RfidRepository
import kotlinx.coroutines.flow.Flow

class RfidRepositoryImpl(
    private val dao: RfidTagDao
) : RfidRepository {

    override fun getTags(): Flow<List<RfidTagEntity>> = dao.getAllTags()

    override suspend fun processScannedTag(epc: String, rssi: Int, antenna: Int) {
        // Lógica de negocio: si el tag ya existe se recupera para actualizar conteo/RSSI, si no se crea nuevo
        val existingTag = dao.getTagsBySyncStatus(SyncStatus.PENDING).find { it.epc == epc }
        val newCount = (existingTag?.readCount ?: 0) + 1

        val updatedTag = RfidTagEntity(
            epc = epc,
            rssi = rssi,
            antenna = antenna,
            readCount = newCount,
            lastSeenTimestamp = System.currentTimeMillis(),
            syncStatus = SyncStatus.PENDING
        )

        dao.upsertTag(updatedTag)
    }

    override suspend fun getPendingSyncTags(): List<RfidTagEntity> {
        return dao.getTagsBySyncStatus(SyncStatus.PENDING)
    }

    override suspend fun markAsSynced(epcs: List<String>) {
        dao.updateSyncStatus(epcs, SyncStatus.SYNCED)
    }

    override suspend fun clearTags() {
        dao.clearAll()
    }
}