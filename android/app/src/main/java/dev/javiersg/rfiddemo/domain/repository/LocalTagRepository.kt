package dev.javiersg.rfiddemo.domain.repository

import dev.javiersg.rfiddemo.data.local.entity.TagEntity
import kotlinx.coroutines.flow.Flow

interface LocalTagRepository {
    fun getTags(): Flow<List<TagEntity>>

    suspend fun processScannedTag(
        epc: String,
        rssi: Int,
        antenna: Int,
    )

    suspend fun getPendingSyncTags(): List<TagEntity>

    suspend fun markAsSyncing(epcs: List<String>)

    suspend fun markAsSynced(epcs: List<String>)

    suspend fun markAsFailed(epcs: List<String>)

    suspend fun clearTags()
}
