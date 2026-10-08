package dev.javiersg.rfiddemo.data.hardware

import dev.javiersg.rfiddemo.domain.model.ReaderStatus
import dev.javiersg.rfiddemo.domain.model.RfidTag
import dev.javiersg.rfiddemo.domain.repository.IRfidReader
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.random.Random

class MockRfidReader : IRfidReader {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val _readerStatus = MutableStateFlow(ReaderStatus.DISCONNECTED)
    override val readerStatus: StateFlow<ReaderStatus> = _readerStatus.asStateFlow()

    // Buffer con DROP_OLDEST: un colector lento (UI) nunca bloquea la emisión de ráfaga.
    private val _tagFlow = MutableSharedFlow<RfidTag>(
        replay = 0,
        extraBufferCapacity = 64,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    override val tagFlow: Flow<RfidTag> = _tagFlow.asSharedFlow()

    private var scanJob: Job? = null

    // Pool de EPCs ya leídos: los lectores reales releen los mismos tags muchas veces.
    private val epcPool = mutableListOf<String>()

    override suspend fun connect() {
        if (_readerStatus.value == ReaderStatus.CONNECTED ||
            _readerStatus.value == ReaderStatus.SCANNING
        ) return

        _readerStatus.value = ReaderStatus.CONNECTING
        try {
            delay(HANDSHAKE_DELAY_MS)
            _readerStatus.value = ReaderStatus.CONNECTED
        } catch (e: CancellationException) {
            // Si el caller se cancela a mitad del handshake, no dejamos el estado colgado.
            _readerStatus.value = ReaderStatus.DISCONNECTED
            throw e
        }
    }

    override suspend fun disconnect() {
        stopScanning()
        _readerStatus.value = ReaderStatus.DISCONNECTED
    }

    override suspend fun startScanning() {
        if (_readerStatus.value != ReaderStatus.CONNECTED) return

        _readerStatus.value = ReaderStatus.SCANNING
        scanJob = scope.launch {
            while (isActive) {
                repeat(Random.nextInt(MIN_BURST, MAX_BURST + 1)) {
                    _tagFlow.emit(
                        RfidTag(
                            epc = nextEpc(),
                            rssi = Random.nextInt(RSSI_MIN, RSSI_MAX)
                        )
                    )
                    delay(Random.nextLong(TAG_SPACING_MIN_MS, TAG_SPACING_MAX_MS))
                }
                delay(Random.nextLong(BURST_GAP_MIN_MS, BURST_GAP_MAX_MS))
            }
        }
    }

    override suspend fun stopScanning() {
        val job = scanJob ?: return
        // cancelAndJoin garantiza que ninguna emisión quede en vuelo tras volver a CONNECTED.
        job.cancelAndJoin()
        scanJob = null
        if (_readerStatus.value == ReaderStatus.SCANNING) {
            _readerStatus.value = ReaderStatus.CONNECTED
        }
    }

    /**
     * Relee tags del pool (comportamiento real del hardware) o genera uno nuevo.
     * 70% releo / 30% tag nuevo para ejercitar la deduplicación del ViewModel.
     */
    private fun nextEpc(): String {
        if (epcPool.isNotEmpty() && Random.nextInt(100) < RELEAD_PERCENT) {
            return epcPool.random()
        }
        val suffix = (0 until EPC_SUFFIX_BYTES)
            .joinToString("") { Random.nextInt(256).toString(16).uppercase().padStart(2, '0') }
        return (EPC_HEADER + suffix).also { epcPool.add(it) }
    }

    companion object {
        private const val HANDSHAKE_DELAY_MS = 800L
        private const val MIN_BURST = 1
        private const val MAX_BURST = 3
        private const val TAG_SPACING_MIN_MS = 30L
        private const val TAG_SPACING_MAX_MS = 90L
        private const val BURST_GAP_MIN_MS = 150L
        private const val BURST_GAP_MAX_MS = 450L
        private const val RSSI_MIN = -75
        private const val RSSI_MAX = -35
        private const val RELEAD_PERCENT = 70
        private const val EPC_HEADER = "E28011700000020"
        private const val EPC_SUFFIX_BYTES = 9
    }
}
