package dev.javiersg.rfiddemo.domain.hardware

import kotlinx.coroutines.flow.Flow

interface RfidReaderService {
    val connectionState: Flow<ReaderConnectionState>
    val scannedTags: Flow<ScannedTagEvent>

    suspend fun connect(): Boolean
    suspend fun disconnect()
    suspend fun startScanning()
    suspend fun stopScanning()
    suspend fun setAntennaPower(powerLevel: Int) // e.g. 10 a 30 dBm
}

data class ScannedTagEvent(
    val epc: String,
    val rssi: Int,
    val antenna: Int = 1
)