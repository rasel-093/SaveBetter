package com.example.savebetter.core.data.remote.firebase.dto

import androidx.annotation.Keep

/**
 * Firebase Firestore DTO for categories.
 */
@Keep
data class CategoryFirestoreDto(
    val id: String = "",
    val userId: String = "",
    val nameKey: String? = null,
    val customName: String? = null,
    val icon: String = "",
    val colorToken: String = "",
    val isDefault: Boolean = false,
    val createdAtEpochMilli: Long = 0L,
    val updatedAtEpochMilli: Long = 0L,
    val isDeleted: Boolean = false
)
