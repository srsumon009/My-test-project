package com.zakattracker.app.ui.years

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zakattracker.app.data.model.ZakatYear
import com.zakattracker.app.data.repository.ZakatRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class YearRow(val year: ZakatYear, val totalPaid: Double)

class ZakatYearsViewModel(private val repository: ZakatRepository) : ViewModel() {

    val years: StateFlow<List<YearRow>> = combine(
        repository.getAllYears(),
        repository.getAllPayments()
    ) { years, payments ->
        years.map { year ->
            YearRow(year, payments.filter { it.zakatYearId == year.id }.sumOf { it.amount })
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun delete(year: ZakatYear) {
        viewModelScope.launch { repository.deleteYear(year) }
    }
}
