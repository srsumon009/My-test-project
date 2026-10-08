package com.zakattracker.app.ui.reports

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zakattracker.app.data.model.Category
import com.zakattracker.app.data.model.Payment
import com.zakattracker.app.data.model.ZakatYear
import com.zakattracker.app.data.repository.ZakatRepository
import com.zakattracker.app.util.todayEpochDay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class YearSummary(val year: ZakatYear, val paid: Double) {
    val due: Double get() = (year.totalPayable - paid).coerceAtLeast(0.0)
}

data class CategorySlice(val categoryName: String, val total: Double)

data class ReportsUiState(
    val yearSummaries: List<YearSummary> = emptyList(),
    val selectedYearId: Long? = null,
    val selectedYearBreakdown: List<CategorySlice> = emptyList(),
    val rangeStart: Long = todayEpochDay() - 30,
    val rangeEnd: Long = todayEpochDay(),
    val rangeTotal: Double = 0.0,
    val rangePayments: List<Pair<Payment, String>> = emptyList()
)

private data class RawData(
    val years: List<ZakatYear>,
    val payments: List<Payment>,
    val categories: List<Category>
)

class ReportsViewModel(private val repository: ZakatRepository) : ViewModel() {

    private val selectedYearId = MutableStateFlow<Long?>(null)
    private val rangeStart = MutableStateFlow(todayEpochDay() - 30)
    private val rangeEnd = MutableStateFlow(todayEpochDay())

    private val rawData = combine(
        repository.getAllYears(), repository.getAllPayments(), repository.getAllCategories()
    ) { years, payments, categories -> RawData(years, payments, categories) }

    val uiState: StateFlow<ReportsUiState> = combine(
        rawData, selectedYearId, rangeStart, rangeEnd
    ) { data, selectedId, start, end ->
        buildState(data, selectedId, start, end)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ReportsUiState())

    fun selectYear(yearId: Long) {
        selectedYearId.value = yearId
    }

    fun setRange(start: Long, end: Long) {
        rangeStart.value = start
        rangeEnd.value = end
    }

    private fun buildState(data: RawData, selectedId: Long?, start: Long, end: Long): ReportsUiState {
        val nameById = data.categories.associate { it.id to it.name }
        val paidByYear = data.years.associate { year ->
            year.id to data.payments.filter { it.zakatYearId == year.id }.sumOf { it.amount }
        }
        val summaries = data.years
            .sortedByDescending { it.startDateEpochDay }
            .map { YearSummary(it, paidByYear[it.id] ?: 0.0) }

        val effectiveSelectedId = selectedId ?: summaries.firstOrNull()?.year?.id
        val breakdown = data.payments
            .filter { it.zakatYearId == effectiveSelectedId }
            .groupBy { it.categoryId }
            .map { (categoryId, list) -> CategorySlice(nameById[categoryId] ?: "Uncategorized", list.sumOf { it.amount }) }
            .sortedByDescending { it.total }

        val inRange = data.payments.filter { it.paymentDateEpochDay in start..end }
        val rangeRows = inRange
            .sortedByDescending { it.paymentDateEpochDay }
            .map { it to (nameById[it.categoryId] ?: "Uncategorized") }

        return ReportsUiState(
            yearSummaries = summaries,
            selectedYearId = effectiveSelectedId,
            selectedYearBreakdown = breakdown,
            rangeStart = start,
            rangeEnd = end,
            rangeTotal = inRange.sumOf { it.amount },
            rangePayments = rangeRows
        )
    }
}
