package com.zakattracker.app.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.zakattracker.app.data.model.ZakatYear
import kotlinx.coroutines.flow.Flow

@Dao
interface ZakatYearDao {

    @Query("SELECT * FROM zakat_year ORDER BY startDateEpochDay DESC")
    fun getAll(): Flow<List<ZakatYear>>

    @Query("SELECT * FROM zakat_year WHERE id = :id")
    fun getById(id: Long): Flow<ZakatYear?>

    @Query("SELECT * FROM zakat_year WHERE id = :id")
    suspend fun getByIdOnce(id: Long): ZakatYear?

    @Insert
    suspend fun insert(zakatYear: ZakatYear): Long

    @Update
    suspend fun update(zakatYear: ZakatYear)

    @Delete
    suspend fun delete(zakatYear: ZakatYear)
}
