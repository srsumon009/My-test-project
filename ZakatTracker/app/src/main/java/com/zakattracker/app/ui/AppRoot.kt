package com.zakattracker.app.ui

import androidx.biometric.BiometricPrompt
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.zakattracker.app.ui.changepin.ChangePinScreen
import com.zakattracker.app.ui.navigation.MainScaffold
import com.zakattracker.app.ui.pinlock.PinLockScreen

/** How long the app can sit in the background (e.g. while the user picks a Drive folder from
 * a share sheet) before it re-locks on return. A security-relevant app re-locks eventually,
 * but not so aggressively that routine export/import flows keep kicking the user back to the
 * PIN screen. */
private const val RELOCK_AFTER_BACKGROUND_MS = 30_000L

/**
 * Top-level state machine for the whole app: locked (PIN/biometric) -> forced PIN change
 * (only right after the very first successful unlock with the default PIN) -> the real app.
 */
@Composable
fun AppRoot() {
    val container = rememberAppContainer()
    val context = LocalContext.current
    val activity = context as? FragmentActivity

    var isLocked by remember { mutableStateOf(true) }
    var forceChangePin by remember { mutableStateOf(false) }
    var lastBackgroundedAt by remember { mutableLongStateOf(0L) }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_STOP -> lastBackgroundedAt = System.currentTimeMillis()
                Lifecycle.Event.ON_START -> {
                    val wasBackgroundedLongEnough =
                        lastBackgroundedAt != 0L &&
                            System.currentTimeMillis() - lastBackgroundedAt > RELOCK_AFTER_BACKGROUND_MS
                    if (container.pinManager.requirePinOnResume && wasBackgroundedLongEnough && !isLocked) {
                        isLocked = true
                    }
                }
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    fun showBiometricPrompt() {
        val hostActivity = activity ?: return
        val executor = ContextCompat.getMainExecutor(hostActivity)
        val prompt = BiometricPrompt(
            hostActivity,
            executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    isLocked = false
                }
            }
        )
        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Unlock Zakat Tracker")
            .setSubtitle("Use your fingerprint to continue")
            .setNegativeButtonText("Use PIN instead")
            .build()
        prompt.authenticate(promptInfo)
    }

    when {
        isLocked -> PinLockScreen(
            onUnlocked = { stillDefaultPin ->
                isLocked = false
                if (stillDefaultPin) forceChangePin = true
            },
            onUseBiometric = { showBiometricPrompt() }
        )
        forceChangePin -> ChangePinScreen(
            forced = true,
            onDone = { forceChangePin = false },
            onCancel = { /* no-op: the forced step cannot be skipped */ }
        )
        else -> MainScaffold()
    }
}
