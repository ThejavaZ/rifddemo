package dev.javiersg.rfiddemo.data.hardware

import android.content.Context
import dev.javiersg.rfiddemo.data.ZebraRfidManager
import dev.javiersg.rfiddemo.domain.hardware.ReaderConnectionState
import dev.javiersg.rfiddemo.domain.hardware.RfidReaderService
import dev.javiersg.rfiddemo.domain.hardware.ScannedTagEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class ZebraRfidServiceImpl(
    private val context: Context,
    private val zebraManager: ZebraRfidManager = ZebraRfidManager(context)
) : RfidReaderService {

    private val scope = CoroutineScope(Dispatchers.IO)
    private var reconnectJob: Job? = null

    private val _connectionState = MutableStateFlow<ReaderConnectionState>(ReaderConnectionState.Disconnected)
    override val connectionState: StateFlow<ReaderConnectionState> = _connectionState.asStateFlow()

    override val scannedTags: Flow<ScannedTagEvent> = zebraManager.tagReadFlow.map { epc ->
        ScannedTagEvent(epc = epc, rssi = 0)
    }

    fun init() {
        zebraManager.initSdk()
    }

    override suspend fun connect(): Boolean {
        return try {
            _connectionState.value = ReaderConnectionState.Connecting
            zebraManager.connectFirstAvailableReader()

            if (zebraManager.connectionState.value.startsWith("Conectado")) {
                val currentDeviceName = zebraManager.connectionState.value.removePrefix("Conectado a ").ifBlank { "Zebra RFID Reader" }
                _connectionState.value = ReaderConnectionState.Connected(readerName = currentDeviceName)
                true
            } else {
                _connectionState.value = ReaderConnectionState.Disconnected
                false
            }
        } catch (e: Exception) {
            _connectionState.value = ReaderConnectionState.Error(e.message ?: "Error al conectar")
            false
        }
    }

    override suspend fun disconnect() {
        try {
            zebraManager.disconnect()
        } finally {
            _connectionState.value = ReaderConnectionState.Disconnected
        }
    }

    override suspend fun startScanning() {
        zebraManager.performInventory()
    }

    override suspend fun stopScanning() {
        zebraManager.stopInventory()
    }

    override suspend fun setAntennaPower(powerLevel: Int) {
        // Reservado para la configuración de potencia del SDK
    }

    fun connectWithRetry(maxRetries: Int = 3) {
        reconnectJob?.cancel()
        reconnectJob = scope.launch {
            var currentAttempt = 0
            var connected = false

            while (currentAttempt < maxRetries && !connected) {
                connected = connect()
                if (!connected) {
                    currentAttempt++
                    delay(2000)
                }
            }
        }
    }

    fun onPause() {
        scope.launch { stopScanning() }
    }

    fun onResume() {
        if (_connectionState.value is ReaderConnectionState.Disconnected ||
            _connectionState.value is ReaderConnectionState.Error) {
            connectWithRetry()
        }
    }

    fun release() {
        reconnectJob?.cancel()
        scope.launch { disconnect() }
    }
}