package dev.javiersg.rfiddemo.ui.reader

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.javiersg.rfiddemo.diagnostics.DiagnosticLogger
import dev.javiersg.rfiddemo.domain.model.ReaderStatus
import dev.javiersg.rfiddemo.domain.usecase.ConnectReaderUseCase
import dev.javiersg.rfiddemo.domain.usecase.DisconnectReaderUseCase
import dev.javiersg.rfiddemo.domain.usecase.ObserveReaderStatusUseCase
import dev.javiersg.rfiddemo.domain.usecase.ReadTagsUseCase
import dev.javiersg.rfiddemo.domain.usecase.StartReadingUseCase
import dev.javiersg.rfiddemo.domain.usecase.StopReadingUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ReaderViewModel
    @Inject
    constructor(
        private val readTags: ReadTagsUseCase,
        private val startReading: StartReadingUseCase,
        private val stopReading: StopReadingUseCase,
        private val connectReader: ConnectReaderUseCase,
        private val disconnectReader: DisconnectReaderUseCase,
        private val observeReaderStatus: ObserveReaderStatusUseCase,
        private val logger: DiagnosticLogger,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow(ReaderUiState())
        val uiState: StateFlow<ReaderUiState> = _uiState.asStateFlow()

        init {
            viewModelScope.launch {
                observeReaderStatus().collect { status ->
                    logger.log("reader status=$status")
                    _uiState.update {
                        it.copy(
                            status = status,
                            errorMessage = if (status == ReaderStatus.ERROR) READER_ERROR_MESSAGE else null,
                        )
                    }
                }
            }
            viewModelScope.launch {
                readTags().collect { newTag ->
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
                            totalCount = currentState.totalCount + 1,
                        )
                    }
                }
            }
        }

        fun onConnect() {
            viewModelScope.launch { connectReader() }
        }

        fun onDisconnect() {
            viewModelScope.launch { disconnectReader() }
        }

        fun onStartScan() {
            viewModelScope.launch { startReading() }
        }

        fun onStopScan() {
            viewModelScope.launch { stopReading() }
        }

        fun clearTags() {
            _uiState.update { it.copy(tags = emptyList(), totalCount = 0) }
        }

        // Libera el lector cuando la UI del Reader muere (scope efímero: viewModelScope ya cancelado).
        override fun onCleared() {
            CoroutineScope(Dispatchers.IO).launch {
                runCatching {
                    stopReading()
                    disconnectReader()
                }
            }
        }

        private companion object {
            const val READER_ERROR_MESSAGE = "Error de comunicación con el lector"
        }
    }
