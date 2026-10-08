package com.zakattracker.app.ui.changepin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.zakattracker.app.ui.SimpleViewModelFactory
import com.zakattracker.app.ui.components.NumericKeypad
import com.zakattracker.app.ui.components.PinDots
import com.zakattracker.app.ui.rememberAppContainer

private const val PIN_LENGTH = 6

/**
 * Two-step "enter new PIN, then confirm it" flow. When [forced] is true (the very first login
 * while the default 009900 PIN is still active) the screen has no way back — the user must
 * finish setting a real PIN before the app will let them in.
 */
@Composable
fun ChangePinScreen(
    forced: Boolean,
    onDone: () -> Unit,
    onCancel: () -> Unit
) {
    val container = rememberAppContainer()
    val viewModel: ChangePinViewModel = viewModel(
        factory = SimpleViewModelFactory { ChangePinViewModel(container.pinManager) }
    )

    var firstEntry by remember { mutableStateOf<String?>(null) }
    var entered by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    val isConfirmStep = firstEntry != null

    fun reset() {
        firstEntry = null
        entered = ""
    }

    fun onDigit(digit: String) {
        if (entered.length >= PIN_LENGTH) return
        error = null
        entered += digit
        if (entered.length == PIN_LENGTH) {
            if (!isConfirmStep) {
                firstEntry = entered
                entered = ""
            } else {
                if (entered == firstEntry) {
                    viewModel.setPin(entered)
                    onDone()
                } else {
                    error = "PINs didn't match, start again"
                    reset()
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = if (forced) "Set your own PIN" else "Change PIN",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(8.dp))
        if (forced) {
            Text(
                text = "For your security, replace the default PIN with one only you know.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(12.dp))
        }
        Text(
            text = error ?: if (isConfirmStep) "Confirm your new PIN" else "Enter a new 6-digit PIN",
            style = MaterialTheme.typography.bodyMedium,
            color = if (error != null) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(24.dp))

        PinDots(length = PIN_LENGTH, filledCount = entered.length)

        Spacer(modifier = Modifier.height(40.dp))

        NumericKeypad(
            onDigit = ::onDigit,
            onBackspace = { if (entered.isNotEmpty()) entered = entered.dropLast(1) }
        )

        if (!forced) {
            Spacer(modifier = Modifier.height(24.dp))
            TextButton(onClick = onCancel) { Text("Cancel") }
        }
    }
}
