package com.example.savebetter.core.data.remote.rest.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * REST API DTO for expense transactions.
 *
 * Money is formatted as Long minor units (poisha / cents) to prevent floating-point loss.
 * Timestamps are standard ISO-8601 strings.
 */
@Serializable
data class RestExpenseDto(
    @SerialName("id") val id: String,
    @SerialName("user_id") val userId: String,
    @SerialName("amount_minor") val amountMinor: Long,
    @SerialName("category_id") val categoryId: String,
    @SerialName("note") val note: String? = null,
    @SerialName("date") val date: String,
    @SerialName("created_at") val createdAt: String,
    @SerialName("updated_at") val updatedAt: String,
    @SerialName("is_deleted") val isDeleted: Boolean = false,
    @SerialName("deleted_at") val deletedAt: String? = null
)
