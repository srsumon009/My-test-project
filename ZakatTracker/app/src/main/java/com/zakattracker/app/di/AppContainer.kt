package com.zakattracker.app.di

import android.content.Context
import com.zakattracker.app.data.backup.DatabaseBackupManager
import com.zakattracker.app.data.backup.ReportExporter
import com.zakattracker.app.data.db.AppDatabase
import com.zakattracker.app.data.repository.ZakatRepository
import com.zakattracker.app.security.PinManager

/**
 * A minimal, hand-rolled dependency container. The app is small enough that a DI framework
 * would add more ceremony than it saves; every screen reaches its dependencies through this
 * single object, obtained from [com.zakattracker.app.ZakatApplication].
 */
class AppContainer(private val appContext: Context) {

    val pinManager: PinManager by lazy { PinManager(appContext) }
    val backupManager: DatabaseBackupManager by lazy { DatabaseBackupManager(appContext) }
    val reportExporter: ReportExporter by lazy { ReportExporter(appContext) }

    // Rebuilt lazily so a fresh AppDatabase instance (after an import) is picked up
    // automatically the next time a repository call is made.
    val repository: ZakatRepository
        get() {
            val db = AppDatabase.getInstance(appContext)
            return ZakatRepository(db.zakatYearDao(), db.categoryDao(), db.paymentDao())
        }
}
