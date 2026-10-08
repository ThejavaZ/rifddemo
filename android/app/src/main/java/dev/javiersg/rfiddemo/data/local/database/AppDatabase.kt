package dev.javiersg.rfiddemo.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import dev.javiersg.rfiddemo.data.local.dao.RfidTagDao
import dev.javiersg.rfiddemo.data.local.entity.RfidTagEntity

@Database(
    entities = [RfidTagEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun rfidTagDao(): RfidTagDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "rfid_demo_db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}