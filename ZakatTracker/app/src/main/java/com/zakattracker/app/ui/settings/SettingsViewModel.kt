package com.zakattracker.app.ui.settings

import android.content.Intent
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zakattracker.app.data.backup.DatabaseBackupManager
import com.zakattracker.app.data.backup.ImportResult
import com.zakattracker.app.security.PinManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class SettingsUiState(
    val requirePinOnResume: Boolean,
    val biometricEnabled: Boolean,
    val lastBackupAtMillis: Long?
)

class SettingsViewModel(
    private val pinManager: PinManager,
    private val backupManager: DatabaseBackupManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        SettingsUiState(
            requirePinOnResume = pinManager.requirePinOnResume,
            biometricEnabled = pinManager.biometricEnabled,
            lastBackupAtMillis = backupManager.lastBackupAtMillis()
        )
    )
    val uiState: StateFlow<SettingsUiState> = _uiState

    fun setRequirePinOnResume(value: Boolean) {
        pinManager.requirePinOnResume = value
        _uiState.value = _uiState.value.copy(requirePinOnResume = value)
    }

    fun setBiometricEnabled(value: Boolean) {
        pinManager.biometricEnabled = value
        _uiState.value = _uiState.value.copy(biometricEnabled = value)
    }

    fun exportDatabase(onReady: (Intent) -> Unit) {
        viewModelScope.launch {
            onReady(backupManager.buildExportIntent())
            _uiState.value = _uiState.value.copy(lastBackupAtMillis = backupManager.lastBackupAtMillis())
        }
    }

    fun importDatabase(uri: Uri, onResult: (ImportResult) -> Unit) {
        viewModelScope.launch { onResult(backupManager.importFrom(uri)) }
    }
}
