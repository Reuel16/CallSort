package com.reuel.callsort.ui.screens.board

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.reuel.callsort.data.repository.BackupRepository
import com.reuel.callsort.data.repository.ContactRepository
import com.reuel.callsort.data.local.entities.ContactEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class BoardViewModel(
    private val contactRepository: ContactRepository,
    private val backupRepository: BackupRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _statusMessage = MutableStateFlow<String?>(null)
    val statusMessage: StateFlow<String?> = _statusMessage

    val filteredContacts: StateFlow<List<ContactEntity>> = combine(
        contactRepository.allContacts,
        _searchQuery
    ) { contacts, query ->
        if (query.isBlank()) {
            contacts
        } else {
            contacts.filter { it.displayName.contains(query, ignoreCase = true) }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun onSearchQueryChanged(newQuery: String) {
        _searchQuery.value = newQuery
    }

    fun exportData() {
        viewModelScope.launch {
            backupRepository.exportData()
                .onSuccess { file -> _statusMessage.value = "Exported to ${file.name}" }
                .onFailure { error -> _statusMessage.value = "Export failed: ${error.message}" }
        }
    }

    fun importData() {
        viewModelScope.launch {
            backupRepository.importData()
                .onSuccess { _statusMessage.value = "Import successful" }
                .onFailure { error -> _statusMessage.value = "Import failed: ${error.message}" }
        }
    }

    fun clearStatusMessage() {
        _statusMessage.value = null
    }
}
