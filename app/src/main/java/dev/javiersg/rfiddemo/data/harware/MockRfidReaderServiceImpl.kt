package dev.javiersg.rfiddemo.data.hardware

import dev.javiersg.rfiddemo.domain.hardware.ReaderConnectionState
import dev.javiersg.rfiddemo.domain.hardware.RfidReaderService
import dev.javiersg.rfiddemo.domain.hardware.ScannedTagEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.random.Random

class MockRfidReaderServiceImpl : RfidReaderService {

    private val _connectionState = MutableStateFlow<ReaderConnectionState>(ReaderConnectionState.Disconnected)
    override val connectionState: StateFlow<ReaderConnectionState> = _connectionState.asStateFlow()

    private val _scannedTags = MutableSharedFlow<ScannedTagEvent>()
    override val scannedTags: SharedFlow<ScannedTagEvent> = _scannedTags.asSharedFlow()

    private var scanningJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Default)

    override suspend fun connect(): Boolean {
        _connectionState.value = ReaderConnectionState.Connecting
        delay(1000) // Simular handshake de conexión USB/Bluetooth
        _connectionState.value = ReaderConnectionState.Connected("Zebra RFD8500 (Simulador)")
        return true
    }

    override suspend fun disconnect() {
        stopScanning()
        _connectionState.value = ReaderConnectionState.Disconnected
    }

    override suspend fun startScanning() {
        if (_connectionState.value !is ReaderConnectionState.Connected) return

        scanningJob?.cancel()
        scanningJob = scope.launch {
            while (true) {
                val mockEpc = "E28011900000002000" + Random.nextInt(10, 99)
                val mockRssi = Random.nextInt(-70, -30)
                _scannedTags.emit(ScannedTagEvent(epc = mockEpc, rssi = mockRssi))
                delay(100) // Simular ráfaga de 10 tags por segundo
            }
        }
    }

    override suspend fun stopScanning() {
        scanningJob?.cancel()
        scanningJob = null
    }

    override suspend fun setAntennaPower(powerLevel: Int) {
        // En mock no hace nada, en Zebra ajustará el Transmitter Power dBm
    }
}