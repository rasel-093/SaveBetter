package com.example.savebetter.core.domain.usecase.profile

import com.example.savebetter.core.domain.model.MonthlyTarget
import com.example.savebetter.core.domain.model.SalaryHandRecord
import com.example.savebetter.core.domain.model.SyncState
import com.example.savebetter.core.domain.model.UserProfile
import com.example.savebetter.core.domain.model.WeeklyTarget
import com.example.savebetter.core.domain.repository.CategoryRepository
import com.example.savebetter.core.domain.repository.TargetRepository
import com.example.savebetter.core.domain.repository.UserProfileRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CompleteOnboardingUseCaseTest {

    private val userProfileRepository: UserProfileRepository = mockk(relaxed = true)
    private val categoryRepository: CategoryRepository = mockk(relaxed = true)
    private val targetRepository: TargetRepository = mockk(relaxed = true)
    private val testDispatcher = UnconfinedTestDispatcher()

    private lateinit var useCase: CompleteOnboardingUseCase

    @Before
    fun setUp() {
        coEvery { userProfileRepository.syncUserProfile(any()) } returns Result.success(Unit)
        coEvery { categoryRepository.syncPendingCategories(any()) } returns Result.success(Unit)
        coEvery { targetRepository.syncPendingTargets(any()) } returns Result.success(Unit)

        useCase = CompleteOnboardingUseCase(
            userProfileRepository = userProfileRepository,
            categoryRepository = categoryRepository,
            targetRepository = targetRepository,
            ioDispatcher = testDispatcher
        )
    }

    @Test
    fun `invoke saves profile with onboardingCompleted true and seeds initial targets in Room`() = runTest {
        coEvery { userProfileRepository.getUserProfile("user_123") } returns null

        val profileSlot = slot<UserProfile>()
        coEvery { userProfileRepository.saveUserProfile(capture(profileSlot)) } returns Unit

        val monthlyTargetSlot = slot<MonthlyTarget>()
        coEvery { targetRepository.saveMonthlyTarget(capture(monthlyTargetSlot)) } returns Unit

        val weeklyTargetSlot = slot<WeeklyTarget>()
        coEvery { targetRepository.saveWeeklyTarget(capture(weeklyTargetSlot)) } returns Unit

        val salarySlot = slot<SalaryHandRecord>()
        coEvery { targetRepository.saveSalaryHandRecord(capture(salarySlot)) } returns Unit

        val params = OnboardingParams(
            userId = "user_123",
            name = "Test User",
            email = "test@example.com",
            monthlySalaryMinor = 5000000L,
            monthlyTargetMinor = 3500000L,
            savingGoalMinor = 1000000L,
            weeklyTargetMinor = 850000L,
            preferredLanguage = "en"
        )

        val result = useCase(params)

        assertTrue(result.isSuccess)

        // 1. Verify UserProfile
        coVerify(exactly = 1) { userProfileRepository.saveUserProfile(any()) }
        assertEquals("user_123", profileSlot.captured.id)
        assertEquals("Test User", profileSlot.captured.name)
        assertEquals("test@example.com", profileSlot.captured.email)
        assertEquals(5000000L, profileSlot.captured.monthlySalaryMinor)
        assertTrue(profileSlot.captured.onboardingCompleted)
        assertEquals(SyncState.PENDING, profileSlot.captured.syncStatus)

        // 2. Verify Default Categories seeded
        coVerify(exactly = 1) { categoryRepository.initializeDefaultCategories("user_123") }

        // 3. Verify Monthly Target
        coVerify(exactly = 1) { targetRepository.saveMonthlyTarget(any()) }
        assertEquals("user_123", monthlyTargetSlot.captured.userId)
        assertEquals(3500000L, monthlyTargetSlot.captured.targetAmountMinor)
        assertEquals(1000000L, monthlyTargetSlot.captured.savingGoalMinor)
        assertEquals(SyncState.PENDING, monthlyTargetSlot.captured.syncStatus)

        // 4. Verify Weekly Target
        coVerify(exactly = 1) { targetRepository.saveWeeklyTarget(any()) }
        assertEquals("user_123", weeklyTargetSlot.captured.userId)
        assertEquals(850000L, weeklyTargetSlot.captured.targetAmountMinor)
        assertEquals(SyncState.PENDING, weeklyTargetSlot.captured.syncStatus)

        // 5. Verify Salary Hand Record
        coVerify(exactly = 1) { targetRepository.saveSalaryHandRecord(any()) }
        assertEquals("user_123", salarySlot.captured.userId)
        assertEquals(5000000L, salarySlot.captured.salaryAmountMinor)
        assertEquals(5000000L, salarySlot.captured.handRemainingAmountMinor)
        assertEquals(SyncState.PENDING, salarySlot.captured.syncStatus)
    }
}
