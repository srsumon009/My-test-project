package com.zakattracker.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.zakattracker.app.util.toCurrencyString

/** A rounded, elevated section wrapper used throughout the app for consistent card styling. */
@Composable
fun SectionCard(
    modifier: Modifier = Modifier,
    content: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), content = content)
    }
}

@Composable
fun LabelledAmountRow(label: String, amount: Double, valueColor: Color = MaterialTheme.colorScheme.onSurface) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = amount.toCurrencyString(), style = MaterialTheme.typography.titleMedium, color = valueColor)
    }
}

@Composable
fun ConfirmDialog(
    title: String,
    message: String,
    confirmLabel: String = "Delete",
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { Text(message) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(confirmLabel, color = MaterialTheme.colorScheme.error)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun EmptyState(message: String, modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/** A minimal horizontal-bar breakdown chart with no external charting dependency. */
@Composable
fun HorizontalBreakdownChart(
    items: List<Pair<String, Double>>,
    barColor: Color = MaterialTheme.colorScheme.primary,
    modifier: Modifier = Modifier
) {
    val maxValue = items.maxOfOrNull { it.second }?.takeIf { it > 0 } ?: 1.0
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        items.forEach { (label, value) ->
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(label, style = MaterialTheme.typography.bodySmall)
                    Text(value.toCurrencyString(), style = MaterialTheme.typography.bodySmall)
                }
                Box(
                    modifier = Modifier
                        .padding(top = 4.dp)
                        .fillMaxWidth()
                        .height(10.dp)
                ) {
                    Canvas(modifier = Modifier.fillMaxWidth().height(10.dp)) {
                        val fraction = (value / maxValue).toFloat().coerceIn(0f, 1f)
                        drawRoundRect(
                            color = barColor.copy(alpha = 0.15f),
                            size = size,
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(6.dp.toPx())
                        )
                        drawRoundRect(
                            color = barColor,
                            size = size.copy(width = size.width * fraction),
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(6.dp.toPx())
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SectionDivider() {
    Divider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outlineVariant)
}

/** Row of filled/empty dots showing PIN entry progress. Shared by the lock screen and the
 * change-PIN screen so both look identical. */
@Composable
fun PinDots(length: Int, filledCount: Int) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        repeat(length) { index ->
            val filled = index < filledCount
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .background(
                        color = if (filled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                        shape = CircleShape
                    )
            )
        }
    }
}

/** A 0-9 numeric keypad with backspace, used for PIN entry screens. [leadingSlot] fills the
 * bottom-left cell (e.g. a fingerprint button), and is left empty otherwise. */
@Composable
fun NumericKeypad(
    onDigit: (String) -> Unit,
    onBackspace: () -> Unit,
    leadingSlot: @Composable () -> Unit = { Spacer(modifier = Modifier.size(64.dp)) }
) {
    val rows = listOf(
        listOf("1", "2", "3"),
        listOf("4", "5", "6"),
        listOf("7", "8", "9")
    )
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        rows.forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(28.dp)) {
                row.forEach { digit -> KeypadDigit(label = digit) { onDigit(digit) } }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(28.dp)) {
            Box(modifier = Modifier.size(64.dp), contentAlignment = Alignment.Center) { leadingSlot() }
            KeypadDigit(label = "0") { onDigit("0") }
            Box(
                modifier = Modifier.size(64.dp).clickable(onClick = onBackspace),
                contentAlignment = Alignment.Center
            ) {
                Text("⌫", style = MaterialTheme.typography.titleLarge)
            }
        }
    }
}

@Composable
private fun KeypadDigit(label: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(64.dp)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(text = label, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Medium)
    }
}
