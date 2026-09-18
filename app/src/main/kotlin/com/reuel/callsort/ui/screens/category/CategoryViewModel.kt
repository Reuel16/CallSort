package com.reuel.callsort.ui.screens.category

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.reuel.callsort.data.local.entities.CategoryEntity
import com.reuel.callsort.data.repository.ContactRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class CategoryDialogState(
    val isShowing: Boolean = false,
    val categoryToEdit: CategoryEntity? = null,
    val name: String = "",
    val colorHex: String = "#3F51B5",
    val selectedParentId: Long? = null
)

class CategoryViewModel(
    private val repository: ContactRepository
) : ViewModel() {

    val categories: StateFlow<List<CategoryEntity>> = repository.getAllCategories()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _dialogState = MutableStateFlow(CategoryDialogState())
    val dialogState: StateFlow<CategoryDialogState> = _dialogState.asStateFlow()

    fun openAddDialog(parentId: Long? = null) {
        _dialogState.value = CategoryDialogState(
            isShowing = true,
            selectedParentId = parentId
        )
    }

    fun openEditDialog(category: CategoryEntity) {
        _dialogState.value = CategoryDialogState(
            isShowing = true,
            categoryToEdit = category,
            name = category.name,
            colorHex = category.colorHex,
            selectedParentId = category.parentId
        )
    }

    fun closeDialog() {
        _dialogState.value = CategoryDialogState(isShowing = false)
    }

    fun updateDialogName(name: String) {
        _dialogState.value = _dialogState.value.copy(name = name)
    }

    fun updateDialogColor(colorHex: String) {
        _dialogState.value = _dialogState.value.copy(colorHex = colorHex)
    }

    fun updateDialogParent(parentId: Long?) {
        _dialogState.value = _dialogState.value.copy(selectedParentId = parentId)
    }

    fun saveCategory() {
        val state = _dialogState.value
        if (state.name.isBlank()) return

        viewModelScope.launch {
            if (state.categoryToEdit == null) {
                repository.insertCategory(
                    CategoryEntity(
                        name = state.name.trim(),
                        colorHex = state.colorHex,
                        parentId = state.selectedParentId
                    )
                )
            } else {
                repository.updateCategory(
                    state.categoryToEdit.copy(
                        name = state.name.trim(),
                        colorHex = state.colorHex,
                        parentId = state.selectedParentId
                    )
                )
            }
            closeDialog()
        }
    }

    fun deleteCategory(category: CategoryEntity) {
        viewModelScope.launch {
            repository.deleteCategory(category)
        }
    }
}
