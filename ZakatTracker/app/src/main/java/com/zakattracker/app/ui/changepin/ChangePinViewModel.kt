package com.zakattracker.app.ui.changepin

import androidx.lifecycle.ViewModel
import com.zakattracker.app.security.PinManager

class ChangePinViewModel(private val pinManager: PinManager) : ViewModel() {
    fun setPin(newPin: String) = pinManager.setPin(newPin)
}
