package dev.javiersg.rfiddemo.di

import android.content.Context
import androidx.room.Room
import dev.javiersg.rfiddemo.data.hardware.MockRfidReader
import dev.javiersg.rfiddemo.data.hardware.ZebraRfidServiceImpl
import dev.javiersg.rfiddemo.data.local.database.AppDatabase
import dev.javiersg.rfiddemo.data.remote.InventoryApiClient
import dev.javiersg.rfiddemo.data.repository.LocalTagRepositoryImpl
import dev.javiersg.rfiddemo.data.repository.RfidRepositoryImpl
import dev.javiersg.rfiddemo.data.sync.KtorTagSyncGateway
import dev.javiersg.rfiddemo.data.sync.SyncScheduler
import dev.javiersg.rfiddemo.data.sync.TagSyncGateway
import dev.javiersg.rfiddemo.domain.hardware.RfidReaderService
import dev.javiersg.rfiddemo.domain.repository.IRfidReader
import dev.javiersg.rfiddemo.domain.repository.LocalTagRepository
import dev.javiersg.rfiddemo.domain.repository.RfidRepository

class AppContainer(context: Context) {

    private val appContext = context.applicationContext

    private val database: AppDatabase by lazy {
        Room.databaseBuilder(
            appContext,
            AppDatabase::class.java,
            "rfid_database"
        ).fallbackToDestructiveMigration(dropAllTables = true).build()
    }

    val rfidReaderService: RfidReaderService by lazy {
        ZebraRfidServiceImpl(appContext)
    }

    val rfidReader: IRfidReader by lazy {
        MockRfidReader()
    }

    val inventoryApiClient: InventoryApiClient by lazy {
        InventoryApiClient()
    }

    val tagSyncGateway: TagSyncGateway by lazy {
        KtorTagSyncGateway(inventoryApiClient)
    }

    val syncScheduler: SyncScheduler by lazy {
        SyncScheduler(appContext)
    }

    val localTagRepository: LocalTagRepository by lazy {
        LocalTagRepositoryImpl(
            rfidTagDao = database.tagDao(),
            rfidReaderService = rfidReaderService,
            syncScheduler = syncScheduler
        )
    }

    val rfidRepository: RfidRepository by lazy {
        RfidRepositoryImpl(rfidReader, localTagRepository)
    }
}
