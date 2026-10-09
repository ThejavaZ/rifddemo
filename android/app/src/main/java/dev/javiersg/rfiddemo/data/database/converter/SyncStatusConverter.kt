package dev.javiersg.rfiddemo.data.database.converter

import androidx.room.TypeConverter
import dev.javiersg.rfiddemo.domain.model.SyncStatus

class SyncStatusConverter {
    @TypeConverter
    fun fromSyncStatus(status: SyncStatus): String = status.name

    @TypeConverter
    fun toSyncStatus(value: String): SyncStatus = SyncStatus.valueOf(value)
}
