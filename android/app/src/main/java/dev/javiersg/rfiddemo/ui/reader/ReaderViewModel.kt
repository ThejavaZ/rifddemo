package dev.javiersg.rfiddemo.ui.reader

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.javiersg.rfiddemo.domain.model.ReaderStatus
import dev.javiersg.rfiddemo.domain.model.RfidTag
import dev.javiersg.rfiddemo.domain.repository.IRfidReader
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ReaderViewModel(
    private val rfidReader: IRfidReader
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReaderUiState())
    val uiState: StateFlow<ReaderUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            rfidReader.readerStatus.collect { status ->
                _uiState.update { it.copy(status = status) }
            }
        }
        viewModelScope.launch {
            rfidReader.tagFlow.collect { newTag ->
                _uiState.update { currentState ->
                    val tags = currentState.tags.toMutableList()
                    val existingIndex = tags.indexOfFirst { it.epc == newTag.epc }

                    if (existingIndex != -1) {
                        tags[existingIndex] = newTag
                    } else {
                        tags.add(newTag)
                    }

                    currentState.copy(
                        tags = tags,
                        totalCount = currentState.totalCount + 1
                    )
                }
            }
        }
    }

    fun toggleConnection() {
        viewModelScope.launch {
            when (_uiState.value.status) {
                ReaderStatus.DISCONNECTED, ReaderStatus.ERROR -> rfidReader.connect()
                ReaderStatus.CONNECTED -> rfidReader.disconnect()
                else -> Unit
            }
        }
    }

    fun toggleScanning() {
        viewModelScope.launch {
            when (_uiState.value.status) {
                ReaderStatus.CONNECTED -> rfidReader.startScanning()
                ReaderStatus.SCANNING -> rfidReader.stopScanning()
                else -> Unit
            }
        }
    }

    fun clearTags() {
        _uiState.update { it.copy(tags = emptyList(), totalCount = 0) }
    }
}
