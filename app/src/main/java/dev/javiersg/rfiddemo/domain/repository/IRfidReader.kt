package dev.javiersg.rfiddemo.domain.repository

import dev.javiersg.rfiddemo.domain.model.RfidTag
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
enum class ConnectionState{
    DISCONNECTED,
    CONNECTING,
    CONNECTED,
    ERROR
}

interface IRfidReader {
    /** Estado de la conexión con el dispositivo (Mock o SDK Zebra) */
    val connectionState: StateFlow<ConnectionState>

    /** Stream reactivo de tags detectados en tiempo real */
    val tagStream: Flow<RfidTag>

    /** Conecta con el hardware o simula conexión */
    suspend fun connect(): Result<Unit>

    /** Desconecta el lector */
    suspend fun disconnect()

    /** Inicia la emisión de ráfagas RFID (trigger presionado) */
    suspend fun startScanning()

    /** Detiene la emisión de ráfagas RFID */
    suspend fun stopScanning()
}