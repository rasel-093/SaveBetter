package com.example.savebetter.core.data.repository

import com.example.savebetter.core.data.local.dao.CategoryDao
import com.example.savebetter.core.data.remote.CategoryRemoteDataSource
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Unit tests for [CategoryRepositoryImpl].
 */
class CategoryRepositoryImplTest {

    private val categoryDao: CategoryDao = mockk(relaxed = true)
    private val remoteDataSource: CategoryRemoteDataSource = mockk(relaxed = true)
    private val repository = CategoryRepositoryImpl(categoryDao, remoteDataSource)

    @Test
    fun `initializeDefaultCategories creates exactly 6 generic categories with expected keys`() = runTest {
        io.mockk.coEvery { remoteDataSource.uploadCategory(any()) } returns Result.success(Unit)
        repository.initializeDefaultCategories("user_abc")

        coVerify {
            categoryDao.upsertCategories(match { list ->
                val keys = list.mapNotNull { it.nameKey }.toSet()
                val expectedKeys = setOf(
                    "category_household",
                    "category_health",
                    "category_family",
                    "category_transport",
                    "category_grocery",
                    "category_other"
                )
                list.size == 6 && keys == expectedKeys
            })
        }
    }
}
