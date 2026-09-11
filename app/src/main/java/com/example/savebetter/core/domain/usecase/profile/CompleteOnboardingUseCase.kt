package com.example.savebetter.core.domain.usecase.profile

import com.example.savebetter.core.domain.model.MonthlyTarget
import com.example.savebetter.core.domain.model.SalaryHandRecord
import com.example.savebetter.core.domain.model.SyncState
import com.example.savebetter.core.domain.model.UserProfile
import com.example.savebetter.core.domain.model.WeeklyTarget
import com.example.savebetter.core.domain.repository.CategoryRepository
import com.example.savebetter.core.domain.repository.TargetRepository
import com.example.savebetter.core.domain.repository.UserProfileRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters
import java.util.UUID
import javax.inject.Inject

/**
 * Parameters required to initialize the user's financial profile during onboarding.
 */
data class OnboardingParams(
    val userId: String,
    val name: String? = null,
    val email: String? = null,
    val monthlySalaryMinor: Long,
    val monthlyTargetMinor: Long,
    val savingGoalMinor: Long,
    val weeklyTargetMinor: Long,
    val preferredLanguage: String = "en"
)

/**
 * Completes the onboarding flow:
 * 1. Upserts UserProfile with onboardingCompleted = true and SyncState.PENDING.
 * 2. Seeds initial default categories in Room.
 * 3. Creates the first MonthlyTarget and WeeklyTarget in Room.
 * 4. Creates the first SalaryHandRecord in Room.
 * 5. Asynchronously triggers backend sync without blocking the user interface.
 */
class CompleteOnboardingUseCase @Inject constructor(
    private val userProfileRepository: UserProfileRepository,
    private val categoryRepository: CategoryRepository,
    private val targetRepository: TargetRepository
) {
    internal var ioDispatcher: CoroutineDispatcher = Dispatchers.IO

    internal constructor(
        userProfileRepository: UserProfileRepository,
        categoryRepository: CategoryRepository,
        targetRepository: TargetRepository,
        ioDispatcher: CoroutineDispatcher
    ) : this(userProfileRepository, categoryRepository, targetRepository) {
        this.ioDispatcher = ioDispatcher
    }
    suspend operator fun invoke(params: OnboardingParams): Result<Unit> {
        return try {
            val now = Instant.now()
            val today = LocalDate.now()
            val weekStart = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).toString()
            val weekEnd = today.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY)).toString()
            val month = today.monthValue
            val year = today.year

            // 1. Fetch existing profile to preserve metadata if any
            val existing = userProfileRepository.getUserProfile(params.userId)
            val updatedProfile = UserProfile(
                id = params.userId,
                name = params.name ?: existing?.name,
                email = params.email ?: existing?.email,
                monthlySalaryMinor = params.monthlySalaryMinor,
                preferredLanguage = params.preferredLanguage.ifEmpty { existing?.preferredLanguage ?: "en" },
                onboardingCompleted = true,
                createdAt = existing?.createdAt ?: now,
                updatedAt = now,
                syncStatus = SyncState.PENDING
            )
            userProfileRepository.saveUserProfile(updatedProfile)

            // 2. Initialize 6 default categories
            categoryRepository.initializeDefaultCategories(params.userId)

            // 3. Initial Monthly Target
            val monthlyTarget = MonthlyTarget(
                id = UUID.randomUUID().toString(),
                userId = params.userId,
                month = month,
                year = year,
                targetAmountMinor = params.monthlyTargetMinor,
                savingGoalMinor = params.savingGoalMinor,
                updatedAt = now,
                syncStatus = SyncState.PENDING
            )
            targetRepository.saveMonthlyTarget(monthlyTarget)

            // 4. Initial Weekly Target
            val weeklyTarget = WeeklyTarget(
                id = UUID.randomUUID().toString(),
                userId = params.userId,
                weekStart = weekStart,
                weekEnd = weekEnd,
                targetAmountMinor = params.weeklyTargetMinor,
                updatedAt = now,
                syncStatus = SyncState.PENDING
            )
            targetRepository.saveWeeklyTarget(weeklyTarget)

            // 5. Initial Salary-in-Hand record
            val salaryRecord = SalaryHandRecord(
                id = UUID.randomUUID().toString(),
                userId = params.userId,
                month = month,
                year = year,
                salaryAmountMinor = params.monthlySalaryMinor,
                handRemainingAmountMinor = params.monthlySalaryMinor,
                updatedAt = now,
                syncStatus = SyncState.PENDING
            )
            targetRepository.saveSalaryHandRecord(salaryRecord)

            // 6. Asynchronous sync trigger (non-blocking)
            CoroutineScope(ioDispatcher).launch {
                try {
                    userProfileRepository.syncUserProfile(params.userId)
                    categoryRepository.syncPendingCategories(params.userId)
                    targetRepository.syncPendingTargets(params.userId)
                } catch (_: Exception) {
                    // Failures stay PENDING in Room for future sync workers
                }
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
