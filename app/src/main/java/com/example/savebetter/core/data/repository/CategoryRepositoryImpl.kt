package com.example.savebetter.core.data.repository

import com.example.savebetter.core.data.local.dao.CategoryDao
import com.example.savebetter.core.data.mapper.toDomain
import com.example.savebetter.core.data.mapper.toEntity
import com.example.savebetter.core.data.remote.CategoryRemoteDataSource
import com.example.savebetter.core.domain.model.Category
import com.example.savebetter.core.domain.model.SyncState
import com.example.savebetter.core.domain.repository.CategoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Concrete implementation of [CategoryRepository].
 */
@Singleton
class CategoryRepositoryImpl @Inject constructor(
    private val categoryDao: CategoryDao,
    private val remoteDataSource: CategoryRemoteDataSource
) : CategoryRepository {

    override fun observeCategories(userId: String): Flow<List<Category>> =
        categoryDao.observeCategories(userId).map { list -> list.map { it.toDomain() } }

    override suspend fun getCategoryById(id: String): Category? =
        categoryDao.getCategoryById(id)?.toDomain()

    override suspend fun addCategory(category: Category) {
        val pending = category.copy(syncStatus = SyncState.PENDING, updatedAt = Instant.now())
        categoryDao.insertCategory(pending.toEntity())

        remoteDataSource.uploadCategory(pending).onSuccess {
            categoryDao.updateCategory(pending.copy(syncStatus = SyncState.SYNCED).toEntity())
        }
    }

    override suspend fun initializeDefaultCategories(userId: String) {
        val defaults = Category.createDefaultCategories(userId)
        categoryDao.upsertCategories(defaults.map { it.toEntity() })

        for (category in defaults) {
            remoteDataSource.uploadCategory(category).onSuccess {
                categoryDao.updateCategory(category.copy(syncStatus = SyncState.SYNCED).toEntity())
            }
        }
    }

    override suspend fun deleteCategory(id: String) {
        val now = Instant.now()
        val category = categoryDao.getCategoryById(id) ?: return
        categoryDao.softDeleteCategory(id, updatedAt = now, syncStatus = SyncState.PENDING)

        remoteDataSource.deleteCategory(category.userId, id).onSuccess {
            categoryDao.softDeleteCategory(id, updatedAt = now, syncStatus = SyncState.SYNCED)
        }
    }

    override suspend fun syncPendingCategories(userId: String): Result<Unit> = runCatching {
        val pending = categoryDao.getPendingCategories(userId)
        for (item in pending) {
            val domain = item.toDomain()
            if (domain.isDeleted) {
                remoteDataSource.deleteCategory(userId, domain.id).onSuccess {
                    categoryDao.softDeleteCategory(domain.id, Instant.now(), SyncState.SYNCED)
                }
            } else {
                remoteDataSource.uploadCategory(domain).onSuccess {
                    categoryDao.updateCategory(domain.copy(syncStatus = SyncState.SYNCED).toEntity())
                }
            }
        }
    }
}
