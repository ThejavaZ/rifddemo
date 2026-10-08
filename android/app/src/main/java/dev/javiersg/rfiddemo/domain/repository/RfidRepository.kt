package dev.javiersg.rfiddemo.domain.repository

import dev.javiersg.rfiddemo.data.local.SyncStatus
import dev.javiersg.rfiddemo.data.local.entity.RfidTagEntity
import kotlinx.coroutines.flow.Flow

interface RfidRepository {
    fun getTags(): Flow<List<RfidTagEntity>>
    suspend fun processScannedTag(epc: String, rssi: Int, antenna: Int)
    suspend fun getPendingSyncTags(): List<RfidTagEntity>
    suspend fun markAsSynced(epcs: List<String>)
    suspend fun clearTags()
}