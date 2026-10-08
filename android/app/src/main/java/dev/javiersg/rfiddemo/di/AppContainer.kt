package dev.javiersg.rfiddemo.di

import android.content.Context
import androidx.room.Room
import dev.javiersg.rfiddemo.data.hardware.MockRfidReader
import dev.javiersg.rfiddemo.data.hardware.ZebraRfidServiceImpl
import dev.javiersg.rfiddemo.data.local.database.AppDatabase
import dev.javiersg.rfiddemo.data.repository.LocalTagRepositoryImpl
import dev.javiersg.rfiddemo.data.repository.RfidRepositoryImpl
import dev.javiersg.rfiddemo.domain.hardware.RfidReaderService
import dev.javiersg.rfiddemo.domain.repository.IRfidReader
import dev.javiersg.rfiddemo.domain.repository.LocalTagRepository
import dev.javiersg.rfiddemo.domain.repository.RfidRepository

class AppContainer(context: Context) {

    private val database: AppDatabase by lazy {
        Room.databaseBuilder(
            context.applicationContext,
            AppDatabase::class.java,
            "rfid_database"
        ).build()
    }

    val rfidReaderService: RfidReaderService by lazy {
        ZebraRfidServiceImpl(context.applicationContext)
    }

    val rfidReader: IRfidReader by lazy {
        MockRfidReader()
    }

    val rfidRepository: RfidRepository by lazy {
        RfidRepositoryImpl(rfidReader)
    }

    val localTagRepository: LocalTagRepository by lazy {
        LocalTagRepositoryImpl(
            rfidTagDao = database.rfidTagDao(),
            rfidReaderService = rfidReaderService
        )
    }
}
