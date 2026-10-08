package com.zakattracker.app.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.zakattracker.app.ZakatApplication
import com.zakattracker.app.di.AppContainer

/** Resolves the app-wide [AppContainer] from any composable without a DI framework. */
@Composable
fun rememberAppContainer(): AppContainer {
    val context = LocalContext.current.applicationContext as ZakatApplication
    return context.container
}

/** A tiny [ViewModelProvider.Factory] that just invokes the given lambda. Used everywhere a
 * screen's ViewModel needs constructor arguments pulled from [AppContainer]. */
class SimpleViewModelFactory(private val creator: () -> ViewModel) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = creator() as T
}
