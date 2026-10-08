package dev.javiersg.rfiddemo.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import dev.javiersg.rfiddemo.data.local.SyncStatus

@Entity(tableName = "rfid_tags")
data class RfidTagEntity(
    @PrimaryKey
    val epc: String,
    val rssi: Int,
    val antenna: Int,
    val readCount: Int,
    val lastSeenTimestamp: Long = System.currentTimeMillis(),
    val syncStatus: SyncStatus = SyncStatus.PENDING
)