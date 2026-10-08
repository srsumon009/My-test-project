package com.zakattracker.app.ui.payments

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zakattracker.app.data.model.Payment
import com.zakattracker.app.data.model.ZakatYear
import com.zakattracker.app.data.repository.ZakatRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class PaymentRow(val payment: Payment, val categoryName: String)

data class PaymentsUiState(
    val year: ZakatYear? = null,
    val rows: List<PaymentRow> = emptyList(),
    val totalPaid: Double = 0.0,
    val searchQuery: String = ""
)

class PaymentsViewModel(
    private val repository: ZakatRepository,
    private val yearId: Long
) : ViewModel() {

    private val searchQuery = MutableStateFlow("")

    val uiState: StateFlow<PaymentsUiState> = combine(
        repository.getYear(yearId),
        repository.getPaymentsForYear(yearId),
        repository.getAllCategories(),
        searchQuery
    ) { year, payments, categories, query ->
        val nameById = categories.associate { it.id to it.name }
        val rows = payments
            .map { PaymentRow(it, nameById[it.categoryId] ?: "Uncategorized") }
            .filter { row ->
                query.isBlank() ||
                    row.categoryName.contains(query, ignoreCase = true) ||
                    row.payment.recipientName.contains(query, ignoreCase = true) ||
                    row.payment.note.contains(query, ignoreCase = true)
            }
        PaymentsUiState(
            year = year,
            rows = rows,
            totalPaid = payments.sumOf { it.amount },
            searchQuery = query
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), PaymentsUiState())

    fun onSearchChange(query: String) {
        searchQuery.value = query
    }

    fun delete(payment: Payment) {
        viewModelScope.launch { repository.deletePayment(payment) }
    }
}
