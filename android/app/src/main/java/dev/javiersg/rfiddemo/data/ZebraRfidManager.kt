package dev.javiersg.rfiddemo.data

import com.zebra.rfid.api3.HANDHELD_TRIGGER_EVENT_TYPE
import com.zebra.rfid.api3.RfidStatusEvents
import com.zebra.rfid.api3.STATUS_EVENT_TYPE
import com.zebra.rfid.api3.ENUM_TRANSPORT
import com.zebra.rfid.api3.InvalidUsageException
import com.zebra.rfid.api3.OperationFailureException
import com.zebra.rfid.api3.RFIDReader
import com.zebra.rfid.api3.ReaderDevice
import com.zebra.rfid.api3.Readers
import com.zebra.rfid.api3.RfidEventsListener
import com.zebra.rfid.api3.RfidReadEvents
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import android.content.Context

class ZebraRfidManager(private val context: Context) : RfidEventsListener {

    private var readers: Readers? = null
    private var readerDevice: ReaderDevice? = null
    private var reader: RFIDReader? = null

    // Estado de conexión para la UI
    private val _connectionState = MutableStateFlow("Desconectado")
    val connectionState: StateFlow<String> = _connectionState.asStateFlow()

    // Flujo de lecturas de Tags (EPC)
    private val _tagReadFlow = MutableSharedFlow<String>()
    val tagReadFlow: SharedFlow<String> = _tagReadFlow.asSharedFlow()

    fun initSdk() {
        try {
            if (readers == null) {
                // ENUM_TRANSPORT.ALL para detectar Bluetooth, USB y Handhelds integrados
                readers = Readers(context, ENUM_TRANSPORT.ALL)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            _connectionState.value = "Error de inicialización SDK"
        }
    }

    fun connectFirstAvailableReader() {
        try {
            val availableDevices = readers?.GetAvailableRFIDReaderList() ?: return
            if (availableDevices.isNotEmpty()) {
                readerDevice = availableDevices[0]
                reader = readerDevice?.rfidReader

                if (reader != null && !reader!!.isConnected) {
                    reader!!.connect()
                    configureReader()
                    _connectionState.value = "Conectado a ${readerDevice!!.name}"
                }
            } else {
                _connectionState.value = "No se encontraron lectores"
            }
        } catch (e: InvalidUsageException) {
            e.printStackTrace()
        } catch (e: OperationFailureException) {
            e.printStackTrace()
            _connectionState.value = "Error al conectar: ${e.results}"
        }
    }

    private fun configureReader() {
        reader?.let { r ->
            try {
                r.Events.addEventsListener(this)
                r.Events.setHandheldEvent(true)
                r.Events.setTagReadEvent(true)
                r.Events.setAttachTagDataWithReadEvent(true)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun performInventory() {
        try {
            reader?.Actions?.Inventory?.perform()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun stopInventory() {
        try {
            reader?.Actions?.Inventory?.stop()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun disconnect() {
        try {
            reader?.let {
                if (it.isConnected) {
                    it.Events.removeEventsListener(this)
                    it.disconnect()
                }
            }
            _connectionState.value = "Desconectado"
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // Callbacks del SDK de Zebra
    override fun eventReadNotify(e: RfidReadEvents?) {
        val readTags = reader?.Actions?.getReadTags(10)
        readTags?.forEach { tagData ->
            tagData.tagID?.let { epc ->
                _tagReadFlow.tryEmit(epc)
            }
        }
    }

    override fun eventStatusNotify(e: RfidStatusEvents?) {
        val statusData = e?.StatusEventData ?: return

        if (statusData.getStatusEventType() == STATUS_EVENT_TYPE.HANDHELD_TRIGGER_EVENT) {
            val triggerEvent = statusData.HandheldTriggerEventData ?: return

            if (triggerEvent == HANDHELD_TRIGGER_EVENT_TYPE.HANDHELD_TRIGGER_PRESSED) {
                performInventory()
            } else if (triggerEvent == HANDHELD_TRIGGER_EVENT_TYPE.HANDHELD_TRIGGER_RELEASED) {
                stopInventory()
            }
        }
    }
}