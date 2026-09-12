package com.example.savebetter.core.data.remote.rest.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * REST API DTO for categories.
 */
@Serializable
data class RestCategoryDto(
    @SerialName("id") val id: String,
    @SerialName("user_id") val userId: String,
    @SerialName("name_key") val nameKey: String? = null,
    @SerialName("custom_name") val customName: String? = null,
    @SerialName("icon") val icon: String,
    @SerialName("color_token") val colorToken: String,
    @SerialName("is_default") val isDefault: Boolean = false,
    @SerialName("created_at") val createdAt: String,
    @SerialName("updated_at") val updatedAt: String,
    @SerialName("is_deleted") val isDeleted: Boolean = false
)
