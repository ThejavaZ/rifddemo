package dev.javiersg.rfiddemo.ui.reader

import dev.javiersg.rfiddemo.domain.model.ReaderStatus
import dev.javiersg.rfiddemo.domain.model.RfidTag

data class ReaderUiState(
    val status: ReaderStatus = ReaderStatus.DISCONNECTED,
    val tags: List<RfidTag> = emptyList(),
    val totalCount: Int = 0,
    val errorMessage: String? = null,
)
