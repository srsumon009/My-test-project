package com.zakattracker.app

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.fragment.app.FragmentActivity
import com.zakattracker.app.ui.AppRoot
import com.zakattracker.app.ui.theme.ZakatTrackerTheme

/**
 * A [FragmentActivity] (rather than the usual [androidx.activity.ComponentActivity]) because
 * [androidx.biometric.BiometricPrompt] needs a FragmentActivity host to attach its dialog to;
 * FragmentActivity still extends ComponentActivity so Compose's `setContent` works as normal.
 */
class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ZakatTrackerTheme {
                AppRoot()
            }
        }
    }
}
