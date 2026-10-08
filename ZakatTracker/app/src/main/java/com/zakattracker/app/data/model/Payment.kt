package com.zakattracker.app.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * A single Zakat payment/disbursement recorded against a [ZakatYear] and a [Category].
 */
@Entity(
    tableName = "payment",
    foreignKeys = [
        ForeignKey(
            entity = ZakatYear::class,
            parentColumns = ["id"],
            childColumns = ["zakatYearId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Category::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [Index("zakatYearId"), Index("categoryId")]
)
data class Payment(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val zakatYearId: Long,
    val categoryId: Long,
    val amount: Double,
    val paymentDateEpochDay: Long,
    val recipientName: String = "",
    val note: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

/** Projection used by the category-breakdown report. */
data class CategoryTotal(
    val categoryId: Long,
    val categoryName: String,
    val total: Double
)
