package com.example.savebetter.core.domain.model

import java.time.Instant
import java.util.UUID

/**
 * Backend-agnostic domain model for an expense category.
 *
 * Rules:
 * - Default categories specify a [nameKey] referencing a string resource in strings.xml.
 * - Custom user categories specify a [customName] entered by the user.
 * - [customName] is never localized via string resources.
 */
data class Category(
    val id: String,
    val userId: String,
    val nameKey: String? = null,
    val customName: String? = null,
    val icon: String,
    val colorToken: String,
    val isDefault: Boolean = false,
    val createdAt: Instant = Instant.now(),
    val updatedAt: Instant = Instant.now(),
    val syncStatus: SyncState = SyncState.SYNCED,
    val isDeleted: Boolean = false
) {
    companion object {
        /**
         * Generic default categories required by the application specification:
         * 1. Household
         * 2. Health
         * 3. Family
         * 4. Transport
         * 5. Market / Grocery
         * 6. Other
         */
        fun createDefaultCategories(userId: String): List<Category> {
            val now = Instant.now()
            return listOf(
                Category(
                    id = "def_cat_household_$userId",
                    userId = userId,
                    nameKey = "category_household",
                    customName = null,
                    icon = "home",
                    colorToken = "cat2",
                    isDefault = true,
                    createdAt = now,
                    updatedAt = now
                ),
                Category(
                    id = "def_cat_health_$userId",
                    userId = userId,
                    nameKey = "category_health",
                    customName = null,
                    icon = "health",
                    colorToken = "cat5",
                    isDefault = true,
                    createdAt = now,
                    updatedAt = now
                ),
                Category(
                    id = "def_cat_family_$userId",
                    userId = userId,
                    nameKey = "category_family",
                    customName = null,
                    icon = "family",
                    colorToken = "cat4",
                    isDefault = true,
                    createdAt = now,
                    updatedAt = now
                ),
                Category(
                    id = "def_cat_transport_$userId",
                    userId = userId,
                    nameKey = "category_transport",
                    customName = null,
                    icon = "transport",
                    colorToken = "cat6",
                    isDefault = true,
                    createdAt = now,
                    updatedAt = now
                ),
                Category(
                    id = "def_cat_grocery_$userId",
                    userId = userId,
                    nameKey = "category_grocery",
                    customName = null,
                    icon = "grocery",
                    colorToken = "cat1",
                    isDefault = true,
                    createdAt = now,
                    updatedAt = now
                ),
                Category(
                    id = "def_cat_other_$userId",
                    userId = userId,
                    nameKey = "category_other",
                    customName = null,
                    icon = "other",
                    colorToken = "cat3",
                    isDefault = true,
                    createdAt = now,
                    updatedAt = now
                )
            )
        }
    }
}
