package dev.javiersg.rfiddemo.ui.scanner

import dev.javiersg.rfiddemo.data.local.entity.TagEntity

data class RfidUiState(
    val tags: List<TagEntity> = emptyList(),
    val totalReads: Int = 0,
    val uniqueTags: Int = 0,
    val isScanning: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)