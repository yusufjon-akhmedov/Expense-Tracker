package com.yusufjonaxmedov.pennywise.feature.categories

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yusufjonaxmedov.pennywise.core.model.Category
import com.yusufjonaxmedov.pennywise.core.model.TransactionType
import com.yusufjonaxmedov.pennywise.core.ui.EmptyStateCard
import com.yusufjonaxmedov.pennywise.core.ui.SectionHeader
import kotlinx.coroutines.flow.collectLatest

@Composable
fun CategoriesScreen(
    onBack: () -> Unit,
    viewModel: CategoriesViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var editor by remember { mutableStateOf<Category?>(null) }
    var deleting by remember { mutableStateOf<Category?>(null) }

    LaunchedEffect(Unit) {
        viewModel.eventFlow.collectLatest(snackbarHostState::showSnackbar)
    }

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                SectionHeader(
                    title = "Categories",
                    subtitle = "Keep your expense and income labels intentional.",
                    actionLabel = "Back",
                    onActionClick = onBack,
                )
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TransactionType.entries.forEach { type ->
                        FilterChip(
                            selected = state.selectedType == type,
                            onClick = { viewModel.updateType(type) },
                            label = { Text(type.name.lowercase().replaceFirstChar { it.uppercase() }) },
                        )
                    }
                    AssistChip(onClick = { editor = Category(id = 0, name = "", emoji = "✨", colorHex = "#155EEF", type = state.selectedType, isDefault = false) }, label = { Text("Add") })
                }
            }
            if (state.categories.isEmpty()) {
                item {
                    EmptyStateCard(
                        title = "No categories yet",
                        description = "Create a category to keep reports and search meaningful.",
                    )
                }
            } else {
                items(state.categories, key = { it.id }) { category ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Column {
                            Text("${category.emoji} ${category.name}", style = MaterialTheme.typography.titleMedium)
                            Text(category.colorHex, style = MaterialTheme.typography.bodySmall)
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            AssistChip(onClick = { editor = category }, label = { Text("Edit") })
                            AssistChip(onClick = { deleting = category }, label = { Text("Delete") })
                        }
                    }
                }
            }
        }
    }

    editor?.let { category ->
        CategoryEditorDialog(
            category = if (category.id == 0L) null else category,
            onDismiss = { editor = null },
            onSave = { name, emoji, colorHex ->
                viewModel.saveCategory(category.takeIf { it.id != 0L }?.id, name, emoji, colorHex)
                editor = null
            },
        )
    }

    deleting?.let { category ->
        DeleteCategoryDialog(
            category = category,
            categories = state.categories.filter { it.id != category.id },
            onDismiss = { deleting = null },
            onDelete = { replacementId ->
                viewModel.deleteCategory(category.id, replacementId)
                deleting = null
            },
        )
    }
}

@Composable
private fun CategoryEditorDialog(
    category: Category?,
    onDismiss: () -> Unit,
    onSave: (String, String, String) -> Unit,
) {
    var name by remember(category) { mutableStateOf(category?.name.orEmpty()) }
    var emoji by remember(category) { mutableStateOf(category?.emoji ?: "✨") }
    var colorHex by remember(category) { mutableStateOf(category?.colorHex ?: "#155EEF") }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = { Button(onClick = { onSave(name, emoji, colorHex) }) { Text("Save") } },
        dismissButton = { Button(onClick = onDismiss) { Text("Cancel") } },
        title = { Text(if (category == null) "New category" else "Edit category") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Name") })
                OutlinedTextField(value = emoji, onValueChange = { emoji = it }, label = { Text("Emoji") })
                OutlinedTextField(value = colorHex, onValueChange = { colorHex = it }, label = { Text("Color hex") })
            }
        },
    )
}

@Composable
private fun DeleteCategoryDialog(
    category: Category,
    categories: List<Category>,
    onDismiss: () -> Unit,
    onDelete: (Long?) -> Unit,
) {
    var replacementId by remember { mutableStateOf<Long?>(categories.firstOrNull()?.id) }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = { Button(onClick = { onDelete(replacementId) }) { Text("Delete") } },
        dismissButton = { Button(onClick = onDismiss) { Text("Cancel") } },
        title = { Text("Delete ${category.name}?") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("If this category is already in use, choose where those transactions should move.")
                categories.forEach { candidate ->
                    FilterChip(
                        selected = replacementId == candidate.id,
                        onClick = { replacementId = candidate.id },
                        label = { Text("${candidate.emoji} ${candidate.name}") },
                    )
                }
            }
        },
    )
}
