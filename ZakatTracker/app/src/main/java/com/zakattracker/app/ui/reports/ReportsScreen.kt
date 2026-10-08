package com.zakattracker.app.ui.reports

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenu
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.zakattracker.app.ui.SimpleViewModelFactory
import com.zakattracker.app.ui.components.EmptyState
import com.zakattracker.app.ui.components.DateField
import com.zakattracker.app.ui.components.HorizontalBreakdownChart
import com.zakattracker.app.ui.components.SectionCard
import com.zakattracker.app.ui.rememberAppContainer
import com.zakattracker.app.util.toCurrencyString
import com.zakattracker.app.util.toDisplayDate
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen() {
    val container = rememberAppContainer()
    val viewModel: ReportsViewModel = viewModel(
        factory = SimpleViewModelFactory { ReportsViewModel(container.repository) }
    )
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var tab by remember { mutableIntStateOf(0) }
    val tabTitles = listOf("Yearly", "By Category", "Custom Range")

    Scaffold { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            TabRow(selectedTabIndex = tab) {
                tabTitles.forEachIndexed { index, title ->
                    Tab(selected = tab == index, onClick = { tab = index }, text = { Text(title) })
                }
            }

            when (tab) {
                0 -> YearlySummaryTab(state)
                1 -> CategoryBreakdownTab(state, onSelectYear = viewModel::selectYear)
                2 -> CustomRangeTab(
                    state = state,
                    onRangeChange = viewModel::setRange,
                    onExport = {
                        scope.launch {
                            val intent = container.reportExporter.buildCsvExportIntent(
                                state.rangePayments, "zakat_report"
                            )
                            context.startActivity(intent)
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun YearlySummaryTab(state: ReportsUiState) {
    if (state.yearSummaries.isEmpty()) {
        EmptyState("No Zakat years defined yet.")
        return
    }
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(state.yearSummaries) { summary ->
            SectionCard {
                Text(
                    "${summary.year.startDateEpochDay.toDisplayDate()} – ${summary.year.endDateEpochDay.toDisplayDate()}",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Payable", style = MaterialTheme.typography.bodySmall)
                    Text(summary.year.totalPayable.toCurrencyString(), style = MaterialTheme.typography.bodySmall)
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Paid", style = MaterialTheme.typography.bodySmall)
                    Text(summary.paid.toCurrencyString(), style = MaterialTheme.typography.bodySmall)
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Due", style = MaterialTheme.typography.bodySmall)
                    Text(summary.due.toCurrencyString(), style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CategoryBreakdownTab(state: ReportsUiState, onSelectYear: (Long) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val selectedLabel = state.yearSummaries.firstOrNull { it.year.id == state.selectedYearId }
        ?.let { "${it.year.startDateEpochDay.toDisplayDate()} – ${it.year.endDateEpochDay.toDisplayDate()}" }
        ?: "Select a year"

    Column(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        if (state.yearSummaries.isEmpty()) {
            EmptyState("No Zakat years defined yet.")
        } else {
            ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
                OutlinedTextField(
                    value = selectedLabel,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Zakat year") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier.fillMaxWidth().menuAnchor()
                )
                ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    state.yearSummaries.forEach { summary ->
                        DropdownMenuItem(
                            text = { Text("${summary.year.startDateEpochDay.toDisplayDate()} – ${summary.year.endDateEpochDay.toDisplayDate()}") },
                            onClick = { onSelectYear(summary.year.id); expanded = false }
                        )
                    }
                }
            }

            if (state.selectedYearBreakdown.isEmpty()) {
                EmptyState("No payments recorded for this year yet.")
            } else {
                SectionCard {
                    HorizontalBreakdownChart(items = state.selectedYearBreakdown.map { it.categoryName to it.total })
                }
            }
        }
    }
}

@Composable
private fun CustomRangeTab(
    state: ReportsUiState,
    onRangeChange: (start: Long, end: Long) -> Unit,
    onExport: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Column(modifier = Modifier.weight(1f)) {
                DateField(
                    label = "From",
                    epochDay = state.rangeStart,
                    onEpochDayChange = { onRangeChange(it, state.rangeEnd) }
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                DateField(
                    label = "To",
                    epochDay = state.rangeEnd,
                    onEpochDayChange = { onRangeChange(state.rangeStart, it) }
                )
            }
        }

        SectionCard {
            Text("Total in range", style = MaterialTheme.typography.titleMedium)
            Text(state.rangeTotal.toCurrencyString(), style = MaterialTheme.typography.headlineSmall)
        }

        OutlinedButton(onClick = onExport, modifier = Modifier.fillMaxWidth()) {
            Icon(Icons.Filled.Share, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Export as CSV")
        }

        Divider()

        if (state.rangePayments.isEmpty()) {
            EmptyState("No payments in this range.")
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(state.rangePayments) { (payment, categoryName) ->
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Text(categoryName, style = MaterialTheme.typography.bodyMedium)
                            Text(
                                payment.paymentDateEpochDay.toDisplayDate(),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(payment.amount.toCurrencyString(), style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
    }
}
