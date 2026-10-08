package com.zakattracker.app.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.zakattracker.app.data.model.CategoryTotal
import com.zakattracker.app.data.model.Payment
import kotlinx.coroutines.flow.Flow

@Dao
interface PaymentDao {

    @Query("SELECT * FROM payment ORDER BY paymentDateEpochDay DESC, id DESC")
    fun getAll(): Flow<List<Payment>>

    @Query("SELECT * FROM payment WHERE zakatYearId = :yearId ORDER BY paymentDateEpochDay DESC, id DESC")
    fun getByYear(yearId: Long): Flow<List<Payment>>

    @Query("SELECT * FROM payment WHERE id = :id")
    fun getById(id: Long): Flow<Payment?>

    @Query("SELECT * FROM payment WHERE id = :id")
    suspend fun getByIdOnce(id: Long): Payment?

    @Query(
        """
        SELECT c.id AS categoryId, c.name AS categoryName, SUM(p.amount) AS total
        FROM payment p
        INNER JOIN category c ON c.id = p.categoryId
        WHERE p.zakatYearId = :yearId
        GROUP BY c.id
        ORDER BY total DESC
        """
    )
    fun getCategoryTotalsForYear(yearId: Long): Flow<List<CategoryTotal>>

    @Insert
    suspend fun insert(payment: Payment): Long

    @Update
    suspend fun update(payment: Payment)

    @Delete
    suspend fun delete(payment: Payment)
}
