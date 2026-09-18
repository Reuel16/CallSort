package com.reuel.callsort.ui.screens.category

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.SubdirectoryArrowRight
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.reuel.callsort.data.local.entities.CategoryEntity
import com.reuel.callsort.ui.screens.board.parseHexColor

val PRESET_COLORS = listOf(
    "#F44336", "#E91E63", "#9C27B0", "#673AB7",
    "#3F51B5", "#2196F3", "#009688", "#4CAF50",
    "#FF9800", "#795548", "#607D8B"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryManagementScreen(
    viewModel: CategoryViewModel,
    onNavigateBack: () -> Unit
) {
    val categories by viewModel.categories.collectAsState()
    val dialogState by viewModel.dialogState.collectAsState()

    val rootCategories = categories.filter { it.parentId == null }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Manage Categories") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { viewModel.openAddDialog() }) {
                Icon(Icons.Default.Add, contentDescription = "Add Root Category")
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (categories.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No categories yet. Tap + to add one.")
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(rootCategories, key = { it.categoryId }) { rootCategory ->
                        val subcategories = categories.filter { it.parentId == rootCategory.categoryId }

                        CategoryTreeItem(
                            category = rootCategory,
                            subcategories = subcategories,
                            allCategories = categories,
                            onEdit = { viewModel.openEditDialog(it) },
                            onDelete = { viewModel.deleteCategory(it) },
                            onAddSubcategory = { viewModel.openAddDialog(parentId = rootCategory.categoryId) }
                        )
                    }
                }
            }

            if (dialogState.isShowing) {
                AddEditCategoryDialog(
                    state = dialogState,
                    allCategories = categories,
                    onNameChange = { viewModel.updateDialogName(it) },
                    onColorChange = { viewModel.updateDialogColor(it) },
                    onParentChange = { viewModel.updateDialogParent(it) },
                    onSave = { viewModel.saveCategory() },
                    onDismiss = { viewModel.closeDialog() }
                )
            }
        }
    }
}

@Composable
fun CategoryTreeItem(
    category: CategoryEntity,
    subcategories: List<CategoryEntity>,
    allCategories: List<CategoryEntity>,
    onEdit: (CategoryEntity) -> Unit,
    onDelete: (CategoryEntity) -> Unit,
    onAddSubcategory: () -> Unit
) {
    Column {
        CategoryCard(
            category = category,
            isSubcategory = false,
            onEdit = { onEdit(category) },
            onDelete = { onDelete(category) },
            onAddSubcategory = onAddSubcategory
        )

        subcategories.forEach { subcategory ->
            Row(modifier = Modifier.padding(start = 24.dp, top = 4.dp)) {
                Icon(
                    imageVector = Icons.Default.SubdirectoryArrowRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.padding(top = 12.dp, end = 4.dp)
                )
                Box(modifier = Modifier.weight(1f)) {
                    CategoryCard(
                        category = subcategory,
                        isSubcategory = true,
                        onEdit = { onEdit(subcategory) },
                        onDelete = { onDelete(subcategory) },
                        onAddSubcategory = null
                    )
                }
            }
        }
    }
}

@Composable
fun CategoryCard(
    category: CategoryEntity,
    isSubcategory: Boolean,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onAddSubcategory: (() -> Unit)?
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(parseHexColor(category.colorHex))
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = category.name,
                fontWeight = if (isSubcategory) FontWeight.Normal else FontWeight.Bold,
                fontSize = if (isSubcategory) 15.sp else 17.sp,
                modifier = Modifier.weight(1f)
            )

            if (onAddSubcategory != null) {
                IconButton(onClick = onAddSubcategory) {
                    Icon(Icons.Default.Add, contentDescription = "Add Subcategory", tint = MaterialTheme.colorScheme.primary)
                }
            }
            IconButton(onClick = onEdit) {
                Icon(Icons.Default.Edit, contentDescription = "Edit Category")
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete Category", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditCategoryDialog(
    state: CategoryDialogState,
    allCategories: List<CategoryEntity>,
    onNameChange: (String) -> Unit,
    onColorChange: (String) -> Unit,
    onParentChange: (Long?) -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit
) {
    var dropdownExpanded by remember { mutableStateOf(false) }
    val validParents = allCategories.filter { it.categoryId != state.categoryToEdit?.categoryId && it.parentId == null }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(if (state.categoryToEdit == null) "Create Category" else "Edit Category")
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = state.name,
                    onValueChange = onNameChange,
                    label = { Text("Category Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Text("Select Color:", style = MaterialTheme.typography.bodyMedium)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    PRESET_COLORS.take(6).forEach { hex ->
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(parseHexColor(hex))
                                .border(
                                    width = if (state.colorHex == hex) 3.dp else 0.dp,
                                    color = if (state.colorHex == hex) MaterialTheme.colorScheme.onSurface else Color.Transparent,
                                    shape = CircleShape
                                )
                                .clickable { onColorChange(hex) }
                        )
                    }
                }

                // Parent Category Selection
                ExposedDropdownMenuBox(
                    expanded = dropdownExpanded,
                    onExpandedChange = { dropdownExpanded = !dropdownExpanded }
                ) {
                    val currentParent = validParents.find { it.categoryId == state.selectedParentId }
                    OutlinedTextField(
                        value = currentParent?.name ?: "None (Root Category)",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Parent Category") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = dropdownExpanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = dropdownExpanded,
                        onDismissRequest = { dropdownExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("None (Root Category)") },
                            onClick = {
                                onParentChange(null)
                                dropdownExpanded = false
                            }
                        )
                        validParents.forEach { parent ->
                            DropdownMenuItem(
                                text = { Text(parent.name) },
                                onClick = {
                                    onParentChange(parent.categoryId)
                                    dropdownExpanded = false
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onSave, enabled = state.name.isNotBlank()) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
