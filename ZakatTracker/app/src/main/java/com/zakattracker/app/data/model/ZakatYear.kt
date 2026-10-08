package com.zakattracker.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * A single Zakat "year" the user defines for themselves (it need not be exactly 365 days —
 * it is simply the period between the last time Zakat was calculated/paid and the next).
 *
 * Dates are stored as epoch-day [java.time.LocalDate.toEpochDay] values so no custom Room
 * type converter is required.
 */
@Entity(tableName = "zakat_year")
data class ZakatYear(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val startDateEpochDay: Long,
    val endDateEpochDay: Long,
    val totalPayable: Double,
    val note: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
