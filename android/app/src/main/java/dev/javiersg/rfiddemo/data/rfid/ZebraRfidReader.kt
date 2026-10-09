package dev.javiersg.rfiddemo.data.rfid

import android.content.Context
import com.zebra.rfid.api3.ENUM_TRANSPORT
import com.zebra.rfid.api3.HANDHELD_TRIGGER_EVENT_TYPE
import com.zebra.rfid.api3.RFIDReader
import com.zebra.rfid.api3.ReaderDevice
import com.zebra.rfid.api3.Readers
import com.zebra.rfid.api3.RfidEventsListener
import com.zebra.rfid.api3.RfidReadEvents
import com.zebra.rfid.api3.RfidStatusEvents
import com.zebra.rfid.api3.STATUS_EVENT_TYPE
import dev.javiersg.rfiddemo.domain.model.ReaderStatus
import dev.javiersg.rfiddemo.domain.model.RfidTag
import dev.javiersg.rfiddemo.domain.repository.RfidReader
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.withContext

/**
 * Implementación de [RfidReader] sobre el SDK Zebra RFID API3 (AAR rfidapi3lib).
 *
 * Los callbacks del SDK (hilo binder) se encapsulan en [callbackFlow]; [shareIn] mantiene
 * un único puente con el SDK y distribuye las lecturas a todos los collectors (UI, Room)
 * sin bloquear el hilo principal.
 */
class ZebraRfidReader(
    private val context: Context,
) : RfidReader {
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private var readers: Readers? = null
    private var readerDevice: ReaderDevice? = null
    private var reader: RFIDReader? = null

    private val _readerStatus = MutableStateFlow(ReaderStatus.DISCONNECTED)
    override val readerStatus: StateFlow<ReaderStatus> = _readerStatus.asStateFlow()

    // Puente de emisión: se activa mientras el flujo compartido esté colectado (Eagerly).
    private var tagEmitter: ((RfidTag) -> Unit)? = null

    override val tagFlow: Flow<RfidTag> =
        callbackFlow {
            tagEmitter = { tag -> trySend(tag) }
            awaitCancellation()
        }.buffer(
            capacity = TAG_BUFFER_CAPACITY,
            onBufferOverflow = BufferOverflow.DROP_OLDEST,
        ).shareIn(
            scope = serviceScope,
            started = SharingStarted.Eagerly,
            replay = 0,
        )

    private val sdkListener =
        object : RfidEventsListener {
            override fun eventReadNotify(e: RfidReadEvents?) {
                drainReadTags()
            }

            override fun eventStatusNotify(e: RfidStatusEvents?) {
                handleStatusEvent(e)
            }
        }

    override suspend fun connect() {
        if (_readerStatus.value == ReaderStatus.CONNECTED ||
            _readerStatus.value == ReaderStatus.SCANNING
        ) {
            return
        }

        _readerStatus.value = ReaderStatus.CONNECTING
        try {
            withContext(Dispatchers.IO) {
                if (readers == null) {
                    readers = Readers(context, ENUM_TRANSPORT.ALL)
                }
                val available = readers?.GetAvailableRFIDReaderList().orEmpty()
                check(available.isNotEmpty()) { "No se encontraron lectores RFID" }

                readerDevice = available.first()
                reader = readerDevice?.rfidReader
                if (reader?.isConnected != true) {
                    reader?.connect()
                }
                configureEvents()
            }
            _readerStatus.value = ReaderStatus.CONNECTED
        } catch (e: CancellationException) {
            _readerStatus.value = ReaderStatus.DISCONNECTED
            throw e
        } catch (_: Exception) {
            // Sin lector, fallo de conexión o configuración: estado ERROR sin tumbar la app.
            _readerStatus.value = ReaderStatus.ERROR
        }
    }

    override suspend fun disconnect() {
        try {
            withContext(Dispatchers.IO) {
                reader?.let { r ->
                    if (r.isConnected) {
                        r.Events.removeEventsListener(sdkListener)
                        r.disconnect()
                    }
                }
            }
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            // Error de hardware al desconectar: de todas formas soltamos las referencias.
        } finally {
            reader = null
            readerDevice = null
            _readerStatus.value = ReaderStatus.DISCONNECTED
        }
    }

    override suspend fun startScanning() {
        if (_readerStatus.value != ReaderStatus.CONNECTED) return
        val current = reader
        if (current?.isConnected != true) {
            _readerStatus.value = ReaderStatus.ERROR
            return
        }
        try {
            withContext(Dispatchers.IO) { current.Actions.Inventory.perform() }
            _readerStatus.value = ReaderStatus.SCANNING
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            _readerStatus.value = ReaderStatus.ERROR
        }
    }

    override suspend fun stopScanning() {
        if (_readerStatus.value != ReaderStatus.SCANNING) return
        try {
            withContext(Dispatchers.IO) { reader?.Actions?.Inventory?.stop() }
            _readerStatus.value = ReaderStatus.CONNECTED
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            _readerStatus.value = ReaderStatus.ERROR
        }
    }

    private fun configureEvents() {
        reader?.Events?.let { events ->
            events.addEventsListener(sdkListener)
            events.setHandheldEvent(true)
            events.setTagReadEvent(true)
            events.setAttachTagDataWithReadEvent(true)
        }
    }

    private fun drainReadTags() {
        val tags = runCatching { reader?.Actions?.getReadTags(TAG_BATCH_SIZE) }.getOrNull() ?: return
        tags.forEach { data ->
            val epc = data.tagID ?: return@forEach
            tagEmitter?.invoke(
                RfidTag(
                    epc = epc,
                    rssi = data.peakRSSI.toInt(),
                    timestamp = System.currentTimeMillis(),
                ),
            )
        }
    }

    private fun handleStatusEvent(event: RfidStatusEvents?) {
        val statusData = event?.StatusEventData ?: return
        if (statusData.statusEventType != STATUS_EVENT_TYPE.HANDHELD_TRIGGER_EVENT) return

        when (statusData.HandheldTriggerEventData) {
            HANDHELD_TRIGGER_EVENT_TYPE.HANDHELD_TRIGGER_PRESSED -> performInventory()
            HANDHELD_TRIGGER_EVENT_TYPE.HANDHELD_TRIGGER_RELEASED -> stopInventory()
            else -> Unit
        }
    }

    private fun performInventory() {
        runCatching { reader?.Actions?.Inventory?.perform() }
    }

    private fun stopInventory() {
        runCatching { reader?.Actions?.Inventory?.stop() }
    }

    private companion object {
        const val TAG_BUFFER_CAPACITY = 64
        const val TAG_BATCH_SIZE = 10
    }
}
