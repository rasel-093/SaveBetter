package com.example.savebetter.core.domain.usecase.expense

import com.example.savebetter.core.domain.model.Category
import com.example.savebetter.core.domain.model.SyncState
import com.example.savebetter.core.domain.repository.CategoryRepository
import java.time.Instant
import java.util.UUID
import javax.inject.Inject

/**
 * Use case to create a new user-defined custom category.
 */
class CreateCustomCategoryUseCase @Inject constructor(
    private val categoryRepository: CategoryRepository
) {
    suspend operator fun invoke(
        userId: String,
        customName: String,
        colorToken: String = "cat1",
        icon: String = "category"
    ): Result<Category> = runCatching {
        require(customName.isNotBlank()) { "Category name cannot be empty" }

        val now = Instant.now()
        val category = Category(
            id = UUID.randomUUID().toString(),
            userId = userId,
            nameKey = null,
            customName = customName.trim(),
            icon = icon,
            colorToken = colorToken,
            isDefault = false,
            createdAt = now,
            updatedAt = now,
            syncStatus = SyncState.PENDING
        )
        categoryRepository.addCategory(category)
        category
    }
}
