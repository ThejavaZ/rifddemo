package dev.javiersg.rfiddemo.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import dev.javiersg.rfiddemo.data.local.SyncStatus
import dev.javiersg.rfiddemo.data.local.entity.RfidTagEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RfidTagDao {

    @Query("SELECT * FROM rfid_tags ORDER BY lastSeenTimestamp DESC")
    fun getAllTags(): Flow<List<RfidTagEntity>>

    @Query("SELECT * FROM rfid_tags WHERE syncStatus = :status")
    suspend fun getTagsBySyncStatus(status: SyncStatus): List<RfidTagEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertTag(tag: RfidTagEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertTags(tags: List<RfidTagEntity>)

    @Query("UPDATE rfid_tags SET syncStatus = :status WHERE epc IN (:epcs)")
    suspend fun updateSyncStatus(epcs: List<String>, status: SyncStatus)

    @Query("DELETE FROM rfid_tags")
    suspend fun clearAll()
}