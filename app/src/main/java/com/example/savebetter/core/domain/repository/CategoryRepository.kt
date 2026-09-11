package com.example.savebetter.core.domain.repository

import com.example.savebetter.core.domain.model.Category
import kotlinx.coroutines.flow.Flow

/**
 * Backend-agnostic repository interface for categories.
 */
interface CategoryRepository {
    fun observeCategories(userId: String): Flow<List<Category>>
    suspend fun getCategoryById(id: String): Category?
    suspend fun addCategory(category: Category)
    suspend fun initializeDefaultCategories(userId: String)
    suspend fun deleteCategory(id: String)
    suspend fun syncPendingCategories(userId: String): Result<Unit>
}
