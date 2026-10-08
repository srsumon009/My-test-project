package com.zakattracker.app.ui.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.zakattracker.app.ui.SimpleViewModelFactory
import com.zakattracker.app.ui.components.EmptyState
import com.zakattracker.app.ui.components.HorizontalBreakdownChart
import com.zakattracker.app.ui.components.LabelledAmountRow
import com.zakattracker.app.ui.components.SectionCard
import com.zakattracker.app.ui.rememberAppContainer
import com.zakattracker.app.ui.theme.DueAmountColor
import com.zakattracker.app.ui.theme.PaidAmountColor
import com.zakattracker.app.util.toCurrencyString
import com.zakattracker.app.util.toDisplayDate

@Composable
fun DashboardScreen(
    onAddPayment: (yearId: Long) -> Unit,
    onCreateFirstYear: () -> Unit,
    onViewAllPayments: (yearId: Long) -> Unit
) {
    val container = rememberAppContainer()
    val viewModel: DashboardViewModel = viewModel(
        factory = SimpleViewModelFactory { DashboardViewModel(container.repository) }
    )
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        floatingActionButton = {
            state.currentYear?.let { year ->
                FloatingActionButton(onClick = { onAddPayment(year.id) }) {
                    Icon(Icons.Filled.Add, contentDescription = "Add payment")
                }
            }
        }
    ) { padding ->
        when {
            state.isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            state.currentYear == null -> Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    EmptyState("Define your first Zakat year to get started.")
                    Button(onClick = onCreateFirstYear) { Text("Create Zakat Year") }
                }
            }
            else -> DashboardContent(
                state = state,
                padding = padding,
                onViewAllPayments = { state.currentYear?.let { onViewAllPayments(it.id) } }
            )
        }
    }
}

@Composable
private fun DashboardContent(
    state: DashboardUiState,
    padding: PaddingValues,
    onViewAllPayments: () -> Unit
) {
    val year = state.currentYear ?: return
    val progress = if (year.totalPayable > 0) (state.totalPaidCurrent / year.totalPayable).toFloat().coerceIn(0f, 1f) else 0f

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = 16.dp, end = 16.dp, top = padding.calculateTopPadding() + 16.dp, bottom = 96.dp
        ),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            SectionCard {
                Text("Current Zakat Year", style = MaterialTheme.typography.titleMedium)
                Text(
                    "${year.startDateEpochDay.toDisplayDate()} – ${year.endDateEpochDay.toDisplayDate()}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(12.dp))
                LinearProgressIndicator(
                    progress = progress,
                    modifier = Modifier.fillMaxWidth().height(10.dp),
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
                Spacer(Modifier.height(12.dp))
                LabelledAmountRow("Total Payable", year.totalPayable)
                LabelledAmountRow("Total Paid", state.totalPaidCurrent, PaidAmountColor)
                LabelledAmountRow("Total Due", state.totalDueCurrent, DueAmountColor)
            }
        }

        if (state.lastYear != null) {
            item {
                SectionCard {
                    Text("Last Zakat Year", style = MaterialTheme.typography.titleMedium)
                    Text(
                        "${state.lastYear.startDateEpochDay.toDisplayDate()} – ${state.lastYear.endDateEpochDay.toDisplayDate()}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(8.dp))
                    LabelledAmountRow("Total Payable", state.lastYear.totalPayable)
                    LabelledAmountRow("Total Paid", state.totalPaidLast, PaidAmountColor)
                }
            }
        }

        if (state.categoryBreakdown.isNotEmpty()) {
            item {
                SectionCard {
                    Text("By Category", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(12.dp))
                    HorizontalBreakdownChart(
                        items = state.categoryBreakdown.map { it.categoryName to it.total }
                    )
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Recent Payments", style = MaterialTheme.typography.titleMedium)
                TextButton(onClick = onViewAllPayments) { Text("View all") }
            }
        }

        if (state.recentPayments.isEmpty()) {
            item { EmptyState("No payments recorded yet for this year.") }
        } else {
            items(state.recentPayments) { payment ->
                SectionCard {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Text(state.categoryNameFor(payment.categoryId), style = MaterialTheme.typography.titleMedium)
                            Text(
                                payment.paymentDateEpochDay.toDisplayDate(),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(payment.amount.toCurrencyString(), style = MaterialTheme.typography.titleMedium)
                    }
                }
            }
        }
    }
}
