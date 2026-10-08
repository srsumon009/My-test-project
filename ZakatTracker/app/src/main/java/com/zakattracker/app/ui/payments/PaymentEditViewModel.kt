package com.zakattracker.app.ui.payments

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zakattracker.app.data.model.Category
import com.zakattracker.app.data.model.Payment
import com.zakattracker.app.data.repository.ZakatRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class PaymentEditViewModel(
    private val repository: ZakatRepository,
    private val yearId: Long,
    private val paymentId: Long?
) : ViewModel() {

    val activeCategories: StateFlow<List<Category>> =
        repository.getActiveCategories().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _existing = MutableStateFlow<Payment?>(null)
    val existing: StateFlow<Payment?> = _existing

    init {
        if (paymentId != null && paymentId > 0) {
            viewModelScope.launch { _existing.value = repository.getPaymentOnce(paymentId) }
        }
    }

    fun save(
        categoryId: Long,
        amount: Double,
        dateEpochDay: Long,
        recipientName: String,
        note: String,
        onSaved: () -> Unit
    ) {
        viewModelScope.launch {
            val current = _existing.value
            repository.savePayment(
                Payment(
                    id = current?.id ?: 0,
                    zakatYearId = yearId,
                    categoryId = categoryId,
                    amount = amount,
                    paymentDateEpochDay = dateEpochDay,
                    recipientName = recipientName,
                    note = note,
                    createdAt = current?.createdAt ?: System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis()
                )
            )
            onSaved()
        }
    }
}
