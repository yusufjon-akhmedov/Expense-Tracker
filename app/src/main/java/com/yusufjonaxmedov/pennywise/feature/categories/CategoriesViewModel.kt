package com.yusufjonaxmedov.pennywise.feature.categories

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yusufjonaxmedov.pennywise.core.model.Category
import com.yusufjonaxmedov.pennywise.core.model.CategoryDraft
import com.yusufjonaxmedov.pennywise.core.model.TransactionType
import com.yusufjonaxmedov.pennywise.domain.repository.CategoriesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class CategoriesUiState(
    val selectedType: TransactionType = TransactionType.EXPENSE,
    val categories: List<Category> = emptyList(),
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class CategoriesViewModel @Inject constructor(
    private val categoriesRepository: CategoriesRepository,
) : ViewModel() {
    private val selectedType = MutableStateFlow(TransactionType.EXPENSE)
    private val events = MutableSharedFlow<String>()

    val eventFlow = events.asSharedFlow()

    val uiState: StateFlow<CategoriesUiState> = combine(
        selectedType,
        selectedType.flatMapLatest(categoriesRepository::observeCategories),
    ) { type, categories ->
        CategoriesUiState(
            selectedType = type,
            categories = categories,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = CategoriesUiState(),
    )

    fun updateType(type: TransactionType) {
        selectedType.value = type
    }

    fun saveCategory(categoryId: Long?, name: String, emoji: String, colorHex: String) {
        viewModelScope.launch {
            if (name.isBlank()) {
                events.emit("Category name cannot be empty.")
                return@launch
            }
            categoriesRepository.upsertCategory(
                CategoryDraft(
                    id = categoryId,
                    name = name,
                    emoji = emoji.ifBlank { "•" },
                    colorHex = colorHex,
                    type = selectedType.value,
                ),
            )
        }
    }

    fun deleteCategory(categoryId: Long, replacementCategoryId: Long?) {
        viewModelScope.launch {
            runCatching {
                categoriesRepository.deleteCategory(categoryId, replacementCategoryId)
            }.onFailure {
                events.emit(it.message ?: "Unable to delete category.")
            }
        }
    }
}
