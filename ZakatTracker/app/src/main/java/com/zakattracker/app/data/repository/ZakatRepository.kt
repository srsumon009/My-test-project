package com.zakattracker.app.data.repository

import com.zakattracker.app.data.dao.CategoryDao
import com.zakattracker.app.data.dao.PaymentDao
import com.zakattracker.app.data.dao.ZakatYearDao
import com.zakattracker.app.data.model.Category
import com.zakattracker.app.data.model.CategoryTotal
import com.zakattracker.app.data.model.Payment
import com.zakattracker.app.data.model.ZakatYear
import kotlinx.coroutines.flow.Flow

/**
 * Single entry point the UI layer talks to for all data operations. Keeping this as a thin
 * pass-through over the DAOs (rather than spreading DAO references across view models) makes
 * it easy to see the whole read/write surface of the app in one file.
 */
class ZakatRepository(
    private val zakatYearDao: ZakatYearDao,
    private val categoryDao: CategoryDao,
    private val paymentDao: PaymentDao
) {
    // --- Zakat years -------------------------------------------------------------------
    fun getAllYears(): Flow<List<ZakatYear>> = zakatYearDao.getAll()
    fun getYear(id: Long): Flow<ZakatYear?> = zakatYearDao.getById(id)
    suspend fun getYearOnce(id: Long): ZakatYear? = zakatYearDao.getByIdOnce(id)

    suspend fun saveYear(year: ZakatYear): Long =
        if (year.id == 0L) zakatYearDao.insert(year) else {
            zakatYearDao.update(year); year.id
        }

    suspend fun deleteYear(year: ZakatYear) = zakatYearDao.delete(year)

    // --- Categories ----------------------------------------------------------------------
    fun getAllCategories(): Flow<List<Category>> = categoryDao.getAll()
    fun getActiveCategories(): Flow<List<Category>> = categoryDao.getActive()

    suspend fun saveCategory(category: Category): Long =
        if (category.id == 0L) categoryDao.insert(category) else {
            categoryDao.update(category); category.id
        }

    /** Returns true if the category was deleted, false if it is in use and was left alone. */
    suspend fun deleteOrDeactivateCategory(category: Category): Boolean {
        val usageCount = categoryDao.countPaymentsForCategory(category.id)
        return if (usageCount == 0) {
            categoryDao.delete(category)
            true
        } else {
            categoryDao.update(category.copy(isActive = false))
            false
        }
    }

    // --- Payments --------------------------------------------------------------------------
    fun getAllPayments(): Flow<List<Payment>> = paymentDao.getAll()
    fun getPaymentsForYear(yearId: Long): Flow<List<Payment>> = paymentDao.getByYear(yearId)
    fun getPayment(id: Long): Flow<Payment?> = paymentDao.getById(id)
    suspend fun getPaymentOnce(id: Long): Payment? = paymentDao.getByIdOnce(id)
    fun getCategoryTotalsForYear(yearId: Long): Flow<List<CategoryTotal>> =
        paymentDao.getCategoryTotalsForYear(yearId)

    suspend fun savePayment(payment: Payment): Long =
        if (payment.id == 0L) paymentDao.insert(payment) else {
            paymentDao.update(payment); payment.id
        }

    suspend fun deletePayment(payment: Payment) = paymentDao.delete(payment)
}
