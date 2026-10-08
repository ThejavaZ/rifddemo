package dev.javiersg.rfiddemo.domain.hardware

sealed interface ReaderConnectionState {
    data object Disconnected : ReaderConnectionState
    data object Connecting : ReaderConnectionState
    data class Connected(val readerName: String) : ReaderConnectionState
    data class Error(val message: String) : ReaderConnectionState
}