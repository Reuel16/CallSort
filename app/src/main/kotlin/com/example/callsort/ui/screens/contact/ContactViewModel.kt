package com.example.callsort.ui.screens.contact

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.callsort.data.local.entities.CategoryEntity
import com.example.callsort.data.local.entities.ContactEntity
import com.example.callsort.data.repository.ContactRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ContactDetailUiState(
    val contact: ContactEntity? = null,
    val assignedCategoryIds: List<Long> = emptyList()
)

class ContactViewModel(
    private val repository: ContactRepository,
    private val contactId: Long
) : ViewModel() {

    private val _uiState = MutableStateFlow(ContactDetailUiState())
    val uiState: StateFlow<ContactDetailUiState> = _uiState.asStateFlow()

    val allCategories: StateFlow<List<CategoryEntity>> = repository.getAllCategories()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    init {
        loadContactDetails()
        observeAssignedCategories()
    }

    private fun loadContactDetails() {
        viewModelScope.launch {
            val contact = repository.getContactById(contactId)
            _uiState.value = _uiState.value.copy(contact = contact)
        }
    }

    private fun observeAssignedCategories() {
        viewModelScope.launch {
            repository.getCategoryIdsForContact(contactId).collect { categoryIds ->
                _uiState.value = _uiState.value.copy(assignedCategoryIds = categoryIds)
            }
        }
    }

    fun toggleCategoryAssignment(categoryId: Long, isAssigned: Boolean) {
        viewModelScope.launch {
            if (isAssigned) {
                repository.assignContactToCategory(contactId, categoryId)
            } else {
                repository.removeContactFromCategory(contactId, categoryId)
            }
        }
    }
}
