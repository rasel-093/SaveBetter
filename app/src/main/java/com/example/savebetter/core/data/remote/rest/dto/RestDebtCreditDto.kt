package com.example.savebetter.core.data.remote.rest.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * REST API DTO for debts and credits (দেনা-পাওনা).
 */
@Serializable
data class RestDebtCreditDto(
    @SerialName("id") val id: String,
    @SerialName("user_id") val userId: String,
    @SerialName("direction") val direction: String, // "RECEIVABLE" or "PAYABLE"
    @SerialName("person_name") val personName: String,
    @SerialName("amount_minor") val amountMinor: Long,
    @SerialName("note") val note: String? = null,
    @SerialName("date") val date: String,
    @SerialName("due_date") val dueDate: String? = null,
    @SerialName("is_settled") val isSettled: Boolean = false,
    @SerialName("created_at") val createdAt: String,
    @SerialName("updated_at") val updatedAt: String,
    @SerialName("is_deleted") val isDeleted: Boolean = false,
    @SerialName("deleted_at") val deletedAt: String? = null
)
