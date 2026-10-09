package dev.javiersg.rfiddemo.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import dev.javiersg.rfiddemo.data.local.entity.TagEntity
import dev.javiersg.rfiddemo.domain.model.SyncStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface TagDao {

    @Query("SELECT * FROM rfid_tags ORDER BY lastSeenTimestamp DESC")
    fun getAllTags(): Flow<List<TagEntity>>

    @Query("SELECT * FROM rfid_tags WHERE syncStatus = :status")
    suspend fun getTagsBySyncStatus(status: SyncStatus): List<TagEntity>

    @Query("SELECT * FROM rfid_tags WHERE syncStatus != 'SYNCED' ORDER BY lastSeenTimestamp ASC")
    suspend fun getUnsyncedTags(): List<TagEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertTag(tag: TagEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertTags(tags: List<TagEntity>)

    @Query("UPDATE rfid_tags SET syncStatus = 'SYNCING', lastAttempt = :timestamp WHERE epc IN (:epcs)")
    suspend fun markAsSyncing(epcs: List<String>, timestamp: Long)

    @Query("UPDATE rfid_tags SET syncStatus = 'SYNCED', lastAttempt = :timestamp, retryCount = 0 WHERE epc IN (:epcs)")
    suspend fun markAsSynced(epcs: List<String>, timestamp: Long)

    @Query("UPDATE rfid_tags SET syncStatus = 'FAILED', lastAttempt = :timestamp, retryCount = retryCount + 1 WHERE epc IN (:epcs)")
    suspend fun markAsFailed(epcs: List<String>, timestamp: Long)

    @Query("DELETE FROM rfid_tags")
    suspend fun clearAll()
}
