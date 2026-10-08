package dev.javiersg.rfiddemo.ui.scanner

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import dev.javiersg.rfiddemo.domain.repository.LocalTagRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class RfidViewModel(
    private val repository: LocalTagRepository
) : ViewModel() {

    val uiState: StateFlow<RfidUiState> = repository.getTags()
        .map { tagList ->
            RfidUiState(
                tags = tagList,
                totalReads = tagList.sumOf { it.readCount },
                uniqueTags = tagList.size,
                isLoading = false
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = RfidUiState(isLoading = true)
        )

    fun processTagScan(epc: String, rssi: Int, antenna: Int = 1) {
        viewModelScope.launch {
            repository.processScannedTag(epc = epc, rssi = rssi, antenna = antenna)
        }
    }

    fun clearScannedTags() {
        viewModelScope.launch {
            repository.clearTags()
        }
    }

    companion object {
        fun provideFactory(repository: LocalTagRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return RfidViewModel(repository) as T
                }
            }
    }
}