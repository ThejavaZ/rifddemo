package dev.javiersg.rfiddemo.features.inventory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.javiersg.rfiddemo.core.logging.DiagnosticLogger
import dev.javiersg.rfiddemo.data.api.InventoryApiClient
import dev.javiersg.rfiddemo.data.database.entity.TagEntity
import dev.javiersg.rfiddemo.domain.model.InventoryItem
import dev.javiersg.rfiddemo.domain.repository.LocalTagRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class InventoryUiState(
    val allTags: List<TagEntity> = emptyList(),
    val query: String = "",
    val item: InventoryItem? = null,
    val itemLoading: Boolean = false,
    val itemError: String? = null,
) {
    val filteredTags: List<TagEntity>
        get() =
            if (query.isBlank()) {
                allTags
            } else {
                allTags.filter { it.epc.contains(query, ignoreCase = true) }
            }
}

@HiltViewModel
class InventoryViewModel
    @Inject
    constructor(
        private val localTagRepository: LocalTagRepository,
        private val inventoryApiClient: InventoryApiClient,
        private val logger: DiagnosticLogger,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow(InventoryUiState())
        val uiState: StateFlow<InventoryUiState> = _uiState.asStateFlow()

        private var searchJob: Job? = null

        init {
            viewModelScope.launch {
                localTagRepository.getTags().collect { tags ->
                    _uiState.update { it.copy(allTags = tags) }
                }
            }
        }

        fun onQueryChange(query: String) {
            _uiState.update { it.copy(query = query, item = null, itemError = null, itemLoading = false) }
            searchJob?.cancel()
            if (query.isBlank()) return

            searchJob =
                viewModelScope.launch {
                    _uiState.update { it.copy(itemLoading = true) }
                    inventoryApiClient
                        .getInventory(query)
                        .onSuccess { item ->
                            logger.log("inventory hit epc=$query")
                            _uiState.update { it.copy(itemLoading = false, item = item) }
                        }.onFailure {
                            logger.log("inventory miss epc=$query")
                            _uiState.update { it.copy(itemLoading = false, itemError = "No encontrado en inventario") }
                        }
                }
        }
    }
