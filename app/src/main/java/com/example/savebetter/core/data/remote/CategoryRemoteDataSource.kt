package com.example.savebetter.core.data.remote

import com.example.savebetter.core.domain.model.Category

/**
 * Backend-agnostic remote data source interface for categories.
 */
interface CategoryRemoteDataSource {
    suspend fun fetchCategories(userId: String): Result<List<Category>>
    suspend fun uploadCategory(category: Category): Result<Unit>
    suspend fun deleteCategory(userId: String, categoryId: String): Result<Unit>
}
