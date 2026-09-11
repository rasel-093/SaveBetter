package com.example.savebetter.core.data.remote.firebase.dto

import androidx.annotation.Keep

/**
 * Firebase Firestore DTO for expense transactions.
 */
@Keep
data class ExpenseFirestoreDto(
    val id: String = "",
    val userId: String = "",
    val amountMinor: Long = 0L,
    val categoryId: String = "",
    val note: String? = null,
    val dateEpochMilli: Long = 0L,
    val createdAtEpochMilli: Long = 0L,
    val updatedAtEpochMilli: Long = 0L,
    val isDeleted: Boolean = false,
    val deletedAtEpochMilli: Long? = null
)
