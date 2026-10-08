package com.zakattracker.app.ui.settings

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.biometric.BiometricManager
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.zakattracker.app.data.backup.ImportResult
import com.zakattracker.app.ui.SimpleViewModelFactory
import com.zakattracker.app.ui.components.SectionCard
import com.zakattracker.app.ui.rememberAppContainer
import com.zakattracker.app.util.toDisplayDateTime

@Composable
fun SettingsScreen(onChangePin: () -> Unit) {
    val container = rememberAppContainer()
    val viewModel: SettingsViewModel = viewModel(
        factory = SimpleViewModelFactory { SettingsViewModel(container.pinManager, container.backupManager) }
    )
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    val biometricAvailable = remember {
        BiometricManager.from(context).canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG) ==
            BiometricManager.BIOMETRIC_SUCCESS
    }

    var importResultMessage by remember { mutableStateOf<String?>(null) }
    var pendingImportUri by remember { mutableStateOf<android.net.Uri?>(null) }

    val importLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) pendingImportUri = uri
    }

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SectionCard {
                Text("Security", style = MaterialTheme.typography.titleMedium)
                Button(onClick = onChangePin, modifier = Modifier.fillMaxWidth()) { Text("Change PIN") }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Ask for PIN when reopening the app", style = MaterialTheme.typography.bodyMedium)
                    Switch(checked = state.requirePinOnResume, onCheckedChange = viewModel::setRequirePinOnResume)
                }

                if (biometricAvailable) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Unlock with fingerprint", style = MaterialTheme.typography.bodyMedium)
                        Switch(checked = state.biometricEnabled, onCheckedChange = viewModel::setBiometricEnabled)
                    }
                }
            }

            SectionCard {
                Text("Backup", style = MaterialTheme.typography.titleMedium)
                Text(
                    "Export your data as a database file you can save to Gmail, Google Drive, or anywhere else, and import it back later.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    state.lastBackupAtMillis?.let { "Last backup: ${it.toDisplayDateTime()}" } ?: "You haven't exported a backup yet.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Button(
                    onClick = {
                        viewModel.exportDatabase { intent -> context.startActivity(intent) }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Export Database") }

                OutlinedButton(
                    onClick = { importLauncher.launch(arrayOf("*/*")) },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Import Database") }
            }

            SectionCard {
                Text("About", style = MaterialTheme.typography.titleMedium)
                Text("Zakat Tracker", style = MaterialTheme.typography.bodyMedium)
                Text(
                    "Version 1.0.0",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }

    pendingImportUri?.let { uri ->
        AlertDialog(
            onDismissRequest = { pendingImportUri = null },
            title = { Text("Replace all data?") },
            text = { Text("Importing a backup replaces every Zakat year, category and payment currently in the app. This can't be undone. Make sure this is the file you want.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.importDatabase(uri) { result ->
                        importResultMessage = when (result) {
                            is ImportResult.Success -> "Import complete. Please reopen the app to see the restored data."
                            is ImportResult.Failure -> result.message
                        }
                    }
                    pendingImportUri = null
                }) { Text("Replace", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = { TextButton(onClick = { pendingImportUri = null }) { Text("Cancel") } }
        )
    }

    importResultMessage?.let { message ->
        AlertDialog(
            onDismissRequest = { importResultMessage = null },
            title = { Text("Import") },
            text = { Text(message) },
            confirmButton = { TextButton(onClick = { importResultMessage = null }) { Text("OK") } }
        )
    }
}
