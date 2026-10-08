package com.zakattracker.app.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.zakattracker.app.data.model.Category
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {

    @Query("SELECT * FROM category ORDER BY name ASC")
    fun getAll(): Flow<List<Category>>

    @Query("SELECT * FROM category WHERE isActive = 1 ORDER BY name ASC")
    fun getActive(): Flow<List<Category>>

    @Query("SELECT COUNT(*) FROM payment WHERE categoryId = :categoryId")
    suspend fun countPaymentsForCategory(categoryId: Long): Int

    @Insert
    suspend fun insert(category: Category): Long

    @Update
    suspend fun update(category: Category)

    @Delete
    suspend fun delete(category: Category)
}
