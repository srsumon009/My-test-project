package com.zakattracker.app.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zakattracker.app.data.model.Category
import com.zakattracker.app.data.model.Payment
import com.zakattracker.app.data.model.ZakatYear
import com.zakattracker.app.data.repository.ZakatRepository
import com.zakattracker.app.util.todayEpochDay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class CategorySlice(val categoryName: String, val total: Double)

data class DashboardUiState(
    val isLoading: Boolean = true,
    val currentYear: ZakatYear? = null,
    val totalPaidCurrent: Double = 0.0,
    val totalDueCurrent: Double = 0.0,
    val lastYear: ZakatYear? = null,
    val totalPaidLast: Double = 0.0,
    val recentPayments: List<Payment> = emptyList(),
    val categoryNameFor: (Long) -> String = { "" },
    val categoryBreakdown: List<CategorySlice> = emptyList(),
    val yearCount: Int = 0
)

class DashboardViewModel(repository: ZakatRepository) : ViewModel() {

    val uiState: StateFlow<DashboardUiState> = combine(
        repository.getAllYears(),
        repository.getAllPayments(),
        repository.getAllCategories()
    ) { years, payments, categories ->
        buildState(years, payments, categories)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DashboardUiState()
    )

    private fun buildState(
        years: List<ZakatYear>,
        payments: List<Payment>,
        categories: List<Category>
    ): DashboardUiState {
        if (years.isEmpty()) {
            return DashboardUiState(isLoading = false, yearCount = 0)
        }

        val today = todayEpochDay()
        val sortedByStart = years.sortedByDescending { it.startDateEpochDay }
        val current = years.firstOrNull { today in it.startDateEpochDay..it.endDateEpochDay }
            ?: sortedByStart.first()
        val last = sortedByStart.firstOrNull { it.id != current.id }

        val nameById = categories.associate { it.id to it.name }
        fun paidFor(year: ZakatYear) = payments.filter { it.zakatYearId == year.id }.sumOf { it.amount }

        val paidCurrent = paidFor(current)
        val currentYearPayments = payments.filter { it.zakatYearId == current.id }
        val breakdown = currentYearPayments
            .groupBy { it.categoryId }
            .map { (categoryId, list) ->
                CategorySlice(
                    categoryName = nameById[categoryId] ?: "Uncategorized",
                    total = list.sumOf { it.amount }
                )
            }
            .sortedByDescending { it.total }

        return DashboardUiState(
            isLoading = false,
            currentYear = current,
            totalPaidCurrent = paidCurrent,
            totalDueCurrent = (current.totalPayable - paidCurrent).coerceAtLeast(0.0),
            lastYear = last,
            totalPaidLast = last?.let { paidFor(it) } ?: 0.0,
            recentPayments = currentYearPayments.sortedWith(
                compareByDescending<Payment> { it.paymentDateEpochDay }.thenByDescending { it.id }
            ).take(5),
            categoryNameFor = { id -> nameById[id] ?: "Uncategorized" },
            categoryBreakdown = breakdown,
            yearCount = years.size
        )
    }
}
