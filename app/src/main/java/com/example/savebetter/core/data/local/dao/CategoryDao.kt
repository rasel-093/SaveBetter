package com.example.savebetter.core.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import androidx.room.Upsert
import com.example.savebetter.core.data.local.entity.CategoryEntity
import com.example.savebetter.core.domain.model.SyncState
import kotlinx.coroutines.flow.Flow
import java.time.Instant

/**
 * Data access object for the Category entity.
 */
@Dao
interface CategoryDao {

    @Query("SELECT * FROM categories WHERE userId = :userId AND isDeleted = 0 ORDER BY isDefault DESC, createdAt ASC")
    fun observeCategories(userId: String): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM categories WHERE id = :id AND isDeleted = 0 LIMIT 1")
    suspend fun getCategoryById(id: String): CategoryEntity?

    @Query("SELECT * FROM categories WHERE userId = :userId AND syncStatus != 'SYNCED'")
    suspend fun getPendingCategories(userId: String): List<CategoryEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: CategoryEntity)

    @Upsert
    suspend fun upsertCategories(categories: List<CategoryEntity>)

    @Update
    suspend fun updateCategory(category: CategoryEntity)

    @Query("UPDATE categories SET isDeleted = 1, syncStatus = :syncStatus, updatedAt = :updatedAt WHERE id = :id")
    suspend fun softDeleteCategory(id: String, updatedAt: Instant, syncStatus: SyncState)
}
