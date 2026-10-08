package dev.javiersg.rfiddemo.ui.reader

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.javiersg.rfiddemo.data.model.ConnectionState
import dev.javiersg.rfiddemo.data.model.ReaderUiState
import dev.javiersg.rfiddemo.data.model.RfidTag
import dev.javiersg.rfiddemo.data.reader.RfidReader
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ReaderViewModel(
    private val rfidReader: RfidReader
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReaderUiState())
    val uiState: StateFlow<ReaderUiState> = _uiState.asStateFlow()

    init {
        observeReaderEvents()
    }

    private fun observeReaderEvents() {
        viewModelScope.launch {
            rfidReader.connectionState.collect { state ->
                _uiState.update { it.copy(connectionState = state) }
            }
        }

        viewModelScope.launch {
            rfidReader.tagReads.collect { newTag ->
                _uiState.update { currentState ->
                    val updatedTags = currentState.tags.toMutableList()
                    val existingIndex = updatedTags.indexOfFirst { it.epc == newTag.epc }

                    if (existingIndex != -1) {
                        val existing = updatedTags[existingIndex]
                        updatedTags[existingIndex] = existing.copy(
                            rssi = newTag.rssi,
                            peakCount = existing.peakCount + 1,
                            lastSeen = newTag.lastSeen
                        )
                    } else {
                        updatedTags.add(newTag)
                    }

                    currentState.copy(
                        tags = updatedTags,
                        totalReads = currentState.totalReads + 1
                    )
                }
            }
        }
    }

    fun toggleConnection() {
        viewModelScope.launch {
            if (_uiState.value.connectionState == ConnectionState.DISCONNECTED) {
                rfidReader.connect()
            } else {
                rfidReader.disconnect()
            }
        }
    }

    fun toggleScanning() {
        viewModelScope.launch {
            if (_uiState.value.isScanning) {
                rfidReader.stopScanning()
                _uiState.update { it.copy(isScanning = false) }
            } else {
                rfidReader.startScanning()
                _uiState.update { it.copy(isScanning = true) }
            }
        }
    }

    fun clearTags() {
        _uiState.update { it.copy(tags = emptyList(), totalReads = 0) }
    }
}