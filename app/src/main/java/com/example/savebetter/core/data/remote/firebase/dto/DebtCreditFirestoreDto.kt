package com.example.savebetter.core.data.remote.firebase.dto

import androidx.annotation.Keep

/**
 * Firebase Firestore DTO for debt and credit entries.
 */
@Keep
data class DebtCreditFirestoreDto(
    val id: String = "",
    val userId: String = "",
    val direction: String = "RECEIVABLE",
    val personName: String = "",
    val amountMinor: Long = 0L,
    val note: String? = null,
    val dateEpochMilli: Long = 0L,
    val dueDateEpochMilli: Long? = null,
    val isSettled: Boolean = false,
    val createdAtEpochMilli: Long = 0L,
    val updatedAtEpochMilli: Long = 0L,
    val isDeleted: Boolean = false,
    val deletedAtEpochMilli: Long? = null
)
