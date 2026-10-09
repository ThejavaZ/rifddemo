package dev.javiersg.rfiddemo.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import dev.javiersg.rfiddemo.domain.model.SyncStatus

@Entity(
    tableName = "rfid_tags",
    indices = [
        Index(value = ["syncStatus"]),
        Index(value = ["lastSeenTimestamp"])
    ]
)
data class TagEntity(
    @PrimaryKey
    val epc: String,
    val rssi: Int,
    val antenna: Int,
    val readCount: Int,
    val lastSeenTimestamp: Long = System.currentTimeMillis(),
    val syncStatus: SyncStatus = SyncStatus.PENDING,
    val lastAttempt: Long = 0L,
    val retryCount: Int = 0
)
