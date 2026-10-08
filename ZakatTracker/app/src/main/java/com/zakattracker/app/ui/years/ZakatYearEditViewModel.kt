package com.zakattracker.app.ui.years

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zakattracker.app.data.model.ZakatYear
import com.zakattracker.app.data.repository.ZakatRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ZakatYearEditViewModel(
    private val repository: ZakatRepository,
    private val yearId: Long?
) : ViewModel() {

    private val _existing = MutableStateFlow<ZakatYear?>(null)
    val existing: StateFlow<ZakatYear?> = _existing

    init {
        if (yearId != null && yearId > 0) {
            viewModelScope.launch {
                _existing.value = repository.getYearOnce(yearId)
            }
        }
    }

    fun save(
        startEpochDay: Long,
        endEpochDay: Long,
        totalPayable: Double,
        note: String,
        onSaved: () -> Unit
    ) {
        viewModelScope.launch {
            val current = _existing.value
            repository.saveYear(
                ZakatYear(
                    id = current?.id ?: 0,
                    startDateEpochDay = startEpochDay,
                    endDateEpochDay = endEpochDay,
                    totalPayable = totalPayable,
                    note = note,
                    createdAt = current?.createdAt ?: System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis()
                )
            )
            onSaved()
        }
    }
}
