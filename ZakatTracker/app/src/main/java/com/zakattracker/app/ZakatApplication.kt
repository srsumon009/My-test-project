package com.zakattracker.app

import android.app.Application
import com.zakattracker.app.di.AppContainer

class ZakatApplication : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        container.pinManager.ensureInitialized()
    }
}
