package dev.javiersg.rfiddemo.data.model

data class ReaderUiState(
    val connectionState: ConnectionState = ConnectionState.DISCONNECTED,
    val isScanning: Boolean = false,
    val tags: List<RfidTag> = emptyList(),
    val totalReads: Long = 0L
)