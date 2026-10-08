package com.zakattracker.app.ui.pinlock

import androidx.lifecycle.ViewModel
import com.zakattracker.app.security.PinManager

class PinLockViewModel(private val pinManager: PinManager) : ViewModel() {

    val biometricEnabled: Boolean get() = pinManager.biometricEnabled

    fun verify(pin: String): Boolean = pinManager.verifyPin(pin)

    fun isDefaultPinStillActive(): Boolean = pinManager.isDefaultPinStillActive()
}
