package dev.javiersg.rfiddemo.data.reader

import dev.javiersg.rfiddemo.data.model.ConnectionState
import dev.javiersg.rfiddemo.data.model.RfidTag
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

class MockRfidReader : RfidReader {

    private val scope = CoroutineScope(Dispatchers.Default)

    private val _connectionState = MutableStateFlow(ConnectionState.DISCONNECTED)
    override val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()

    private val _tagReads = MutableSharedFlow<RfidTag>(replay = 0)
    override val tagReads: SharedFlow<RfidTag> = _tagReads.asSharedFlow()

    private var scanJob: Job? = null

    // Pool de EPCs simulados para emular escaneo masivo
    private val mockEpcs = listOf(
        "E28011700000020C731A8901",
        "E28011700000020C731A8902",
        "E28011700000020C731A8903",
        "E28011700000020C731A8904",
        "E28011700000020C731A8905",
        "E28011700000020C731A8906",
        "E28011700000020C731A8907",
        "E28011700000020C731A8908"
    )

    override suspend fun connect() {
        if (_connectionState.value == ConnectionState.CONNECTED) return
        _connectionState.value = ConnectionState.CONNECTING
        delay(1000) // Simula handshake hardware
        _connectionState.value = ConnectionState.CONNECTED
    }

    override suspend fun disconnect() {
        stopScanning()
        _connectionState.value = ConnectionState.DISCONNECTED
    }

    override suspend fun startScanning() {
        if (_connectionState.value != ConnectionState.CONNECTED || scanJob?.isActive == true) return

        scanJob = scope.launch {
            while (true) {
                val randomEpc = mockEpcs.random()
                val randomRssi = Random.nextInt(-75, -35)
                val randomAntenna = Random.nextInt(1, 3)

                _tagReads.emit(
                    RfidTag(
                        epc = randomEpc,
                        rssi = randomRssi,
                        antenna = randomAntenna
                    )
                )
                delay(150) // Emula lecturas rápidas en ráfaga (burst rate)
            }
        }
    }

    override suspend fun stopScanning() {
        scanJob?.cancel()
        scanJob = null
    }
}