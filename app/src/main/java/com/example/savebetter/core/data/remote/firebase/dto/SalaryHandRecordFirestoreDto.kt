package com.example.savebetter.core.data.remote.firebase.dto

import androidx.annotation.Keep

/**
 * Firebase Firestore DTO for salary and cash in hand records.
 */
@Keep
data class SalaryHandRecordFirestoreDto(
    val id: String = "",
    val userId: String = "",
    val month: Int = 1,
    val year: Int = 2026,
    val salaryAmountMinor: Long = 0L,
    val handRemainingAmountMinor: Long = 0L,
    val updatedAtEpochMilli: Long = 0L
)
