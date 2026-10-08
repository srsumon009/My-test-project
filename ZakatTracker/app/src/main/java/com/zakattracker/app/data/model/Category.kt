package com.zakattracker.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * A payment category (e.g. "Poor & Needy", "Relatives", "Education"). Categories can be
 * added or renamed at any time. A category already referenced by a payment is only
 * deactivated (hidden from the picker for new entries), never hard-deleted, so historical
 * reports keep their labels.
 */
@Entity(tableName = "category")
data class Category(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)
