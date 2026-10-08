package dev.javiersg.rfiddemo.data.reader

import dev.javiersg.rfiddemo.data.model.ConnectionState
import dev.javiersg.rfiddemo.data.model.RfidTag
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

interface RfidReader {
    val connectionState: StateFlow<ConnectionState>
    val tagReads: SharedFlow<RfidTag>

    suspend fun connect()
    suspend fun disconnect()
    suspend fun startScanning()
    suspend fun stopScanning()
}