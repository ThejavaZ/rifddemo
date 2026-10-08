package dev.javiersg.rfiddemo.data.reader

import dev.javiersg.rfiddemo.domain.model.RfidTag
import dev.javiersg.rfiddemo.domain.repository.ConnectionState
import dev.javiersg.rfiddemo.domain.repository.IRfidReader
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

class MockRfidReader : IRfidReader {
    private val _connectionState = MutableStateFlow(ConnectionState.DISCONNECTED)
    override val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()

    private val _tagStream = MutableSharedFlow<RfidTag>(replay = 0)
    override val tagStream: SharedFlow<RfidTag> = _tagStream.asSharedFlow()

    private var scanJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Default)
    // Pool de EPCs de prueba que simularán etiquetas reales en almacén
    private val mockEpcs = listOf(
        "E28011700000020A12345678",
        "E28011700000020A87654321",
        "E28011700000020AABCDEF12",
        "E28011700000020A99887766",
        "E28011700000020A55443322"
    )

    override suspend fun connect(): Result<Unit> {
        _connectionState.value = ConnectionState.CONNECTING
        delay(800) // Simula la latencia de handshake Bluetooth/USB
        _connectionState.value = ConnectionState.CONNECTED
        return Result.success(Unit)
    }

    override suspend fun disconnect() {
        stopScanning()
        _connectionState.value = ConnectionState.DISCONNECTED
    }

    override suspend fun startScanning() {
        if (_connectionState.value != ConnectionState.CONNECTED) return
        if (scanJob?.isActive == true) return

        scanJob = scope.launch {
            while (true) {
                // Genera una lectura simulada cada 300ms a 700ms
                delay(Random.nextLong(300, 700))
                val randomEpc = mockEpcs.random()
                val randomRssi = Random.nextInt(-75, -35) // dBm típico de RFID

                _tagStream.emit(
                    RfidTag(
                        epc = randomEpc,
                        rssi = randomRssi
                    )
                )
            }
        }
    }

    override suspend fun stopScanning() {
        scanJob?.cancel()
        scanJob = null
    }
}