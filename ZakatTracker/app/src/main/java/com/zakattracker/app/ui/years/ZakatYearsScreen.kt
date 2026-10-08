package com.zakattracker.app.ui.years

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.zakattracker.app.data.model.ZakatYear
import com.zakattracker.app.ui.SimpleViewModelFactory
import com.zakattracker.app.ui.components.ConfirmDialog
import com.zakattracker.app.ui.components.EmptyState
import com.zakattracker.app.ui.components.SectionCard
import com.zakattracker.app.ui.rememberAppContainer
import com.zakattracker.app.ui.theme.DueAmountColor
import com.zakattracker.app.ui.theme.PaidAmountColor
import com.zakattracker.app.util.toCurrencyString
import com.zakattracker.app.util.toDisplayDate

@Composable
fun ZakatYearsScreen(
    onAddYear: () -> Unit,
    onEditYear: (yearId: Long) -> Unit,
    onOpenPayments: (yearId: Long) -> Unit
) {
    val container = rememberAppContainer()
    val viewModel: ZakatYearsViewModel = viewModel(
        factory = SimpleViewModelFactory { ZakatYearsViewModel(container.repository) }
    )
    val years by viewModel.years.collectAsState()
    var pendingDelete by remember { mutableStateOf<ZakatYear?>(null) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = onAddYear) {
                Icon(Icons.Filled.Add, contentDescription = "Add Zakat year")
            }
        }
    ) { padding ->
        if (years.isEmpty()) {
            EmptyState(
                "No Zakat years defined yet. Tap + to create one.",
                modifier = Modifier.fillMaxSize()
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = 16.dp, end = 16.dp, top = padding.calculateTopPadding() + 16.dp, bottom = 96.dp
                ),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(years, key = { it.year.id }) { row ->
                    SectionCard(modifier = Modifier.clickable { onOpenPayments(row.year.id) }) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    "${row.year.startDateEpochDay.toDisplayDate()} – ${row.year.endDateEpochDay.toDisplayDate()}",
                                    style = MaterialTheme.typography.titleMedium
                                )
                                val due = (row.year.totalPayable - row.totalPaid).coerceAtLeast(0.0)
                                Text(
                                    "Paid ${row.totalPaid.toCurrencyString()} of ${row.year.totalPayable.toCurrencyString()}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                if (due > 0) {
                                    Text("Due ${due.toCurrencyString()}", style = MaterialTheme.typography.bodySmall, color = DueAmountColor)
                                } else {
                                    Text("Fully paid", style = MaterialTheme.typography.bodySmall, color = PaidAmountColor)
                                }
                            }
                            IconButton(onClick = { onEditYear(row.year.id) }) {
                                Icon(Icons.Filled.Edit, contentDescription = "Edit year")
                            }
                            IconButton(onClick = { pendingDelete = row.year }) {
                                Icon(Icons.Filled.Delete, contentDescription = "Delete year", tint = MaterialTheme.colorScheme.error)
                            }
                            Icon(Icons.Filled.ChevronRight, contentDescription = null)
                        }
                    }
                }
            }
        }
    }

    pendingDelete?.let { year ->
        ConfirmDialog(
            title = "Delete Zakat year?",
            message = "This also deletes every payment recorded under this year. This can't be undone.",
            onConfirm = {
                viewModel.delete(year)
                pendingDelete = null
            },
            onDismiss = { pendingDelete = null }
        )
    }
}
