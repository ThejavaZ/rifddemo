package dev.javiersg.rfiddemo.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import dev.javiersg.rfiddemo.data.dao.TagDao
import dev.javiersg.rfiddemo.data.database.converter.SyncStatusConverter
import dev.javiersg.rfiddemo.data.database.entity.InventoryEntity
import dev.javiersg.rfiddemo.data.database.entity.TagEntity

@Database(
    entities = [TagEntity::class, InventoryEntity::class],
    version = 2,
    exportSchema = false,
)
@TypeConverters(SyncStatusConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun tagDao(): TagDao
}
