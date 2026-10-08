package com.zakattracker.app.ui.categories

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zakattracker.app.data.model.Category
import com.zakattracker.app.data.repository.ZakatRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CategoriesViewModel(private val repository: ZakatRepository) : ViewModel() {

    val categories: StateFlow<List<Category>> =
        repository.getAllCategories().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun add(name: String) {
        if (name.isBlank()) return
        viewModelScope.launch { repository.saveCategory(Category(name = name.trim())) }
    }

    fun rename(category: Category, newName: String) {
        if (newName.isBlank()) return
        viewModelScope.launch { repository.saveCategory(category.copy(name = newName.trim())) }
    }

    fun setActive(category: Category, isActive: Boolean) {
        viewModelScope.launch { repository.saveCategory(category.copy(isActive = isActive)) }
    }

    /** Returns false (and leaves the category as deactivated instead) if the category is
     * still referenced by at least one payment, so historical reports keep their label. */
    fun delete(category: Category, onResult: (deleted: Boolean) -> Unit) {
        viewModelScope.launch {
            val deleted = repository.deleteOrDeactivateCategory(category)
            onResult(deleted)
        }
    }
}
