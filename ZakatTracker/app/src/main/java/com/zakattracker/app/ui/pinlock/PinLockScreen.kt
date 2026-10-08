package com.zakattracker.app.ui.pinlock

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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

@Composable
fun PinLockScreen(
    onUnlocked: (forceChangePin: Boolean) -> Unit,
    onUseBiometric: () -> Unit
) {
    val container = rememberAppContainer()
    val viewModel: PinLockViewModel = viewModel(
        factory = SimpleViewModelFactory { PinLockViewModel(container.pinManager) }
    )

    var entered by remember { mutableStateOf("") }
    var error by remember { mutableStateOf(false) }

    fun onDigit(digit: String) {
        if (entered.length >= PIN_LENGTH) return
        error = false
        entered += digit
        if (entered.length == PIN_LENGTH) {
            if (viewModel.verify(entered)) {
                onUnlocked(viewModel.isDefaultPinStillActive())
            } else {
                error = true
                entered = ""
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
            text = "Zakat Tracker",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = if (error) "Incorrect PIN, try again" else "Enter your 6-digit PIN",
            style = MaterialTheme.typography.bodyMedium,
            color = if (error) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(24.dp))

        PinDots(length = PIN_LENGTH, filledCount = entered.length)

        Spacer(modifier = Modifier.height(40.dp))

        NumericKeypad(
            onDigit = ::onDigit,
            onBackspace = { if (entered.isNotEmpty()) entered = entered.dropLast(1) },
            leadingSlot = {
                if (viewModel.biometricEnabled) {
                    IconButton(onClick = onUseBiometric) {
                        Icon(
                            imageVector = Icons.Filled.Fingerprint,
                            contentDescription = "Use fingerprint",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        )
    }
}
