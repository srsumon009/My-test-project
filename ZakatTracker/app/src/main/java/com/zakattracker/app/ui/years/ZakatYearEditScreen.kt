package com.zakattracker.app.ui.years

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.zakattracker.app.ui.SimpleViewModelFactory
import com.zakattracker.app.ui.rememberAppContainer
import com.zakattracker.app.ui.components.DateField
import com.zakattracker.app.util.todayEpochDay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ZakatYearEditScreen(
    yearId: Long?,
    onDone: () -> Unit,
    onBack: () -> Unit
) {
    val container = rememberAppContainer()
    val viewModel: ZakatYearEditViewModel = viewModel(
        factory = SimpleViewModelFactory { ZakatYearEditViewModel(container.repository, yearId) }
    )
    val existing by viewModel.existing.collectAsState()

    var startDay by remember { mutableStateOf(todayEpochDay()) }
    var endDay by remember { mutableStateOf(todayEpochDay() + 354) } // ~1 lunar year
    var payableText by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var initialized by remember { mutableStateOf(false) }

    LaunchedEffect(existing) {
        existing?.let {
            startDay = it.startDateEpochDay
            endDay = it.endDateEpochDay
            payableText = if (it.totalPayable == it.totalPayable.toLong().toDouble()) {
                it.totalPayable.toLong().toString()
            } else it.totalPayable.toString()
            note = it.note
            initialized = true
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (yearId != null && yearId > 0) "Edit Zakat Year" else "New Zakat Year") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "Define the period this Zakat calculation covers, and how much is payable in total.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            DateField(label = "Start date", epochDay = startDay, onEpochDayChange = { startDay = it })
            DateField(label = "End date", epochDay = endDay, onEpochDayChange = { endDay = it })

            OutlinedTextField(
                value = payableText,
                onValueChange = { payableText = it.filter { c -> c.isDigit() || c == '.' } },
                label = { Text("Total Zakat payable (৳)") },
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                label = { Text("Note (optional)") },
                modifier = Modifier.fillMaxWidth()
            )

            error?.let {
                Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }

            Button(onClick = {
                val payable = payableText.toDoubleOrNull()
                when {
                    payable == null || payable < 0 -> error = "Enter a valid payable amount."
                    endDay <= startDay -> error = "End date must be after the start date."
                    else -> {
                        error = null
                        viewModel.save(startDay, endDay, payable, note, onDone)
                    }
                }
            }) {
                Text("Save")
            }
        }
    }
}
