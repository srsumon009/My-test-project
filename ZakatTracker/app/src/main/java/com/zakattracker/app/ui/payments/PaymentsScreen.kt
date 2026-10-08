package com.zakattracker.app.ui.payments

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.zakattracker.app.data.model.Payment
import com.zakattracker.app.ui.SimpleViewModelFactory
import com.zakattracker.app.ui.components.ConfirmDialog
import com.zakattracker.app.ui.components.EmptyState
import com.zakattracker.app.ui.components.SectionCard
import com.zakattracker.app.ui.rememberAppContainer
import com.zakattracker.app.util.toCurrencyString
import com.zakattracker.app.util.toDisplayDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentsScreen(
    yearId: Long,
    onBack: () -> Unit,
    onAddPayment: () -> Unit,
    onEditPayment: (paymentId: Long) -> Unit
) {
    val container = rememberAppContainer()
    val viewModel: PaymentsViewModel = viewModel(
        factory = SimpleViewModelFactory { PaymentsViewModel(container.repository, yearId) }
    )
    val state by viewModel.uiState.collectAsState()
    var pendingDelete by remember { mutableStateOf<Payment?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        state.year?.let { "${it.startDateEpochDay.toDisplayDate()} – ${it.endDateEpochDay.toDisplayDate()}" }
                            ?: "Payments"
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, contentDescription = "Back") }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddPayment) {
                Icon(Icons.Filled.Add, contentDescription = "Add payment")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            OutlinedTextField(
                value = state.searchQuery,
                onValueChange = viewModel::onSearchChange,
                placeholder = { Text("Search payments") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)
            )

            Text(
                "Total paid this year: ${state.totalPaid.toCurrencyString()}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )

            if (state.rows.isEmpty()) {
                EmptyState("No payments yet. Tap + to add one.", modifier = Modifier.fillMaxSize())
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(state.rows, key = { it.payment.id }) { row ->
                        SectionCard(modifier = Modifier.clickable { onEditPayment(row.payment.id) }) {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(row.categoryName, style = MaterialTheme.typography.titleMedium)
                                    Text(
                                        row.payment.paymentDateEpochDay.toDisplayDate(),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    if (row.payment.recipientName.isNotBlank()) {
                                        Text(
                                            "To: ${row.payment.recipientName}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                                Text(row.payment.amount.toCurrencyString(), style = MaterialTheme.typography.titleMedium)
                                IconButton(onClick = { pendingDelete = row.payment }) {
                                    Icon(Icons.Filled.Delete, contentDescription = "Delete payment", tint = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    pendingDelete?.let { payment ->
        ConfirmDialog(
            title = "Delete payment?",
            message = "This payment record will be permanently removed.",
            onConfirm = {
                viewModel.delete(payment)
                pendingDelete = null
            },
            onDismiss = { pendingDelete = null }
        )
    }
}
