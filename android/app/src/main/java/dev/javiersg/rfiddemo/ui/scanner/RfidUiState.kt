package dev.javiersg.rfiddemo.ui.scanner

import dev.javiersg.rfiddemo.data.local.entity.RfidTagEntity

data class RfidUiState(
    val tags: List<RfidTagEntity> = emptyList(),
    val totalReads: Int = 0,
    val uniqueTags: Int = 0,
    val isScanning: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)