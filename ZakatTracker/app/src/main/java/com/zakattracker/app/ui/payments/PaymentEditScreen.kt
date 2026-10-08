package com.zakattracker.app.ui.payments

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenu
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.DropdownMenuItem
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
import com.zakattracker.app.ui.components.DateField
import com.zakattracker.app.ui.rememberAppContainer
import com.zakattracker.app.util.todayEpochDay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentEditScreen(
    yearId: Long,
    paymentId: Long?,
    onDone: () -> Unit,
    onBack: () -> Unit
) {
    val container = rememberAppContainer()
    val viewModel: PaymentEditViewModel = viewModel(
        factory = SimpleViewModelFactory { PaymentEditViewModel(container.repository, yearId, paymentId) }
    )
    val categories by viewModel.activeCategories.collectAsState()
    val existing by viewModel.existing.collectAsState()

    var selectedCategoryId by remember { mutableStateOf<Long?>(null) }
    var amountText by remember { mutableStateOf("") }
    var dateEpochDay by remember { mutableStateOf(todayEpochDay()) }
    var recipient by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var dropdownExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(existing) {
        existing?.let {
            selectedCategoryId = it.categoryId
            amountText = if (it.amount == it.amount.toLong().toDouble()) it.amount.toLong().toString() else it.amount.toString()
            dateEpochDay = it.paymentDateEpochDay
            recipient = it.recipientName
            note = it.note
        }
    }

    LaunchedEffect(categories) {
        if (selectedCategoryId == null && categories.isNotEmpty() && existing == null) {
            selectedCategoryId = categories.first().id
        }
    }

    val selectedCategoryName = categories.firstOrNull { it.id == selectedCategoryId }?.name ?: ""

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (paymentId != null && paymentId > 0) "Edit Payment" else "Add Payment") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, contentDescription = "Back") }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (categories.isEmpty()) {
                Text(
                    "Add at least one category first (Categories tab) before recording a payment.",
                    color = MaterialTheme.colorScheme.error
                )
            } else {
                ExposedDropdownMenuBox(
                    expanded = dropdownExpanded,
                    onExpandedChange = { dropdownExpanded = it }
                ) {
                    OutlinedTextField(
                        value = selectedCategoryName,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Category") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = dropdownExpanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = dropdownExpanded,
                        onDismissRequest = { dropdownExpanded = false }
                    ) {
                        categories.forEach { category ->
                            DropdownMenuItem(
                                text = { Text(category.name) },
                                onClick = {
                                    selectedCategoryId = category.id
                                    dropdownExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            OutlinedTextField(
                value = amountText,
                onValueChange = { amountText = it.filter { c -> c.isDigit() || c == '.' } },
                label = { Text("Amount (৳)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth()
            )

            DateField(label = "Payment date", epochDay = dateEpochDay, onEpochDayChange = { dateEpochDay = it })

            OutlinedTextField(
                value = recipient,
                onValueChange = { recipient = it },
                label = { Text("Recipient (optional)") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                label = { Text("Note (optional)") },
                modifier = Modifier.fillMaxWidth()
            )

            error?.let { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) }

            Button(
                onClick = {
                    val amount = amountText.toDoubleOrNull()
                    val categoryId = selectedCategoryId
                    when {
                        categoryId == null -> error = "Choose a category."
                        amount == null || amount <= 0 -> error = "Enter a valid amount."
                        else -> {
                            error = null
                            viewModel.save(categoryId, amount, dateEpochDay, recipient, note, onDone)
                        }
                    }
                },
                enabled = categories.isNotEmpty()
            ) {
                Text("Save Payment")
            }
        }
    }
}
