package com.reuel.callsort.ui.screens.board

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.reuel.callsort.data.local.entities.CategoryEntity
import com.reuel.callsort.data.local.entities.ContactEntity
import com.reuel.callsort.data.repository.ContactRepository
import com.reuel.callsort.domain.usecase.GetCategoryContactsUseCase
import com.reuel.callsort.domain.usecase.SyncContactsUseCase
import com.reuel.callsort.domain.usecase.SyncResult
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class BoardUiState(
    val selectedCategoryId: Long? = null,
    val isSyncing: Boolean = false,
    val syncError: String? = null
)

class BoardViewModel(
    private val repository: ContactRepository,
    private val getCategoryContactsUseCase: GetCategoryContactsUseCase,
    private val syncContactsUseCase: SyncContactsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(BoardUiState())
    val uiState: StateFlow<BoardUiState> = _uiState.asStateFlow()

    // Flow of categories for filter tabs/chips
    val categories: StateFlow<List<CategoryEntity>> = repository.getAllCategories()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Flow of contacts based on selected category (supports upward inheritance)
    @OptIn(ExperimentalCoroutinesApi::class)
    val contacts: StateFlow<List<ContactEntity>> = _uiState
        .flatMapLatest { state ->
            getCategoryContactsUseCase(state.selectedCategoryId)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun selectCategory(categoryId: Long?) {
        _uiState.value = _uiState.value.copy(selectedCategoryId = categoryId)
    }

    fun syncContacts() {
        viewModelScope.launch {
            syncContactsUseCase().collect { result ->
                when (result) {
                    is SyncResult.Loading -> {
                        _uiState.value = _uiState.value.copy(isSyncing = true, syncError = null)
                    }
                    is SyncResult.Success -> {
                        _uiState.value = _uiState.value.copy(isSyncing = false)
                    }
                    is SyncResult.Error -> {
                        _uiState.value = _uiState.value.copy(
                            isSyncing = false,
                            syncError = result.exception.localizedMessage ?: "Failed to sync contacts"
                        )
                    }
                }
            }
        }
    }
}
