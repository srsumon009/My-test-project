package com.zakattracker.app.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.zakattracker.app.data.dao.CategoryDao
import com.zakattracker.app.data.dao.PaymentDao
import com.zakattracker.app.data.dao.ZakatYearDao
import com.zakattracker.app.data.model.Category
import com.zakattracker.app.data.model.Payment
import com.zakattracker.app.data.model.ZakatYear
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

const val DATABASE_NAME = "zakat_tracker.db"

/** Categories seeded the very first time the database is created. The user can rename,
 * add to, or deactivate any of these afterwards. */
val DEFAULT_CATEGORIES = listOf(
    "Poor & Needy",
    "Relatives",
    "Orphans",
    "Education",
    "Medical Aid",
    "Mosque / Madrasa",
    "Other"
)

@Database(
    entities = [ZakatYear::class, Category::class, Payment::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun zakatYearDao(): ZakatYearDao
    abstract fun categoryDao(): CategoryDao
    abstract fun paymentDao(): PaymentDao

    companion object {
        @Volatile
        private var instance: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return instance ?: synchronized(this) {
                instance ?: build(context.applicationContext).also { instance = it }
            }
        }

        private fun build(context: Context): AppDatabase {
            return Room.databaseBuilder(context, AppDatabase::class.java, DATABASE_NAME)
                .addCallback(object : RoomDatabase.Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        // Seed default categories on first run only.
                        CoroutineScope(Dispatchers.IO).launch {
                            val dao = getInstance(context).categoryDao()
                            DEFAULT_CATEGORIES.forEach { name ->
                                dao.insert(Category(name = name))
                            }
                        }
                    }
                })
                .build()
        }

        /**
         * Closes and drops the cached instance so that the *next* call to [getInstance]
         * opens a fresh connection. Used right after a database file has been replaced
         * by an import.
         */
        fun closeAndReset() {
            synchronized(this) {
                instance?.close()
                instance = null
            }
        }
    }
}
