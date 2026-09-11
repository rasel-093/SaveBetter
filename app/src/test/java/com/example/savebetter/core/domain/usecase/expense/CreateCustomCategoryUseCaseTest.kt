package com.example.savebetter.core.domain.usecase.expense

import com.example.savebetter.core.domain.model.Category
import com.example.savebetter.core.domain.model.SyncState
import com.example.savebetter.core.domain.repository.CategoryRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class CreateCustomCategoryUseCaseTest {

    private val categoryRepository: CategoryRepository = mockk(relaxed = true)
    private lateinit var useCase: CreateCustomCategoryUseCase

    @Before
    fun setUp() {
        useCase = CreateCustomCategoryUseCase(categoryRepository)
    }

    @Test
    fun `invoke creates custom category with isDefault false and PENDING syncStatus`() = runTest {
        val categorySlot = slot<Category>()
        coEvery { categoryRepository.addCategory(capture(categorySlot)) } returns Unit

        val result = useCase(
            userId = "user_789",
            customName = "  Books & Learning  ",
            colorToken = "cat4"
        )

        assertTrue(result.isSuccess)
        val created = result.getOrThrow()

        coVerify(exactly = 1) { categoryRepository.addCategory(any()) }

        val captured = categorySlot.captured
        assertEquals("user_789", captured.userId)
        assertEquals("Books & Learning", captured.customName)
        assertNull(captured.nameKey)
        assertEquals("cat4", captured.colorToken)
        assertFalse(captured.isDefault)
        assertEquals(SyncState.PENDING, captured.syncStatus)
        assertNotNull(captured.id)
        assertEquals(captured.id, created.id)
    }

    @Test
    fun `invoke fails when customName is blank`() = runTest {
        val result = useCase(
            userId = "user_789",
            customName = "   ",
            colorToken = "cat1"
        )

        assertTrue(result.isFailure)
        coVerify(exactly = 0) { categoryRepository.addCategory(any()) }
    }
}
