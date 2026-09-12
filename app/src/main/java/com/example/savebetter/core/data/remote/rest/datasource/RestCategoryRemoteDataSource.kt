package com.example.savebetter.core.data.remote.rest.datasource

import com.example.savebetter.core.data.remote.CategoryRemoteDataSource
import com.example.savebetter.core.data.remote.rest.api.SaveBetterRestApi
import com.example.savebetter.core.data.remote.rest.mapper.toDomain
import com.example.savebetter.core.data.remote.rest.mapper.toRestDto
import com.example.savebetter.core.domain.model.Category
import javax.inject.Inject
import javax.inject.Singleton

/**
 * RESTful backend implementation of [CategoryRemoteDataSource].
 */
@Singleton
class RestCategoryRemoteDataSource @Inject constructor(
    private val restApi: SaveBetterRestApi
) : CategoryRemoteDataSource {

    override suspend fun fetchCategories(userId: String): Result<List<Category>> {
        return restApi.fetchCategories(userId).map { dtos ->
            dtos.map { it.toDomain() }
        }
    }

    override suspend fun uploadCategory(category: Category): Result<Unit> {
        return restApi.uploadCategory(category.userId, category.toRestDto())
    }

    override suspend fun deleteCategory(userId: String, categoryId: String): Result<Unit> {
        return restApi.deleteCategory(userId, categoryId)
    }
}
