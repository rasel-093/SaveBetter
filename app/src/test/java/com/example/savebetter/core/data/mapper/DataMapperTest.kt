package com.example.savebetter.core.data.mapper

import com.example.savebetter.core.domain.model.Category
import com.example.savebetter.core.domain.model.DebtCredit
import com.example.savebetter.core.domain.model.DebtDirection
import com.example.savebetter.core.domain.model.Expense
import com.example.savebetter.core.domain.model.MonthlyTarget
import com.example.savebetter.core.domain.model.SalaryHandRecord
import com.example.savebetter.core.domain.model.SyncState
import com.example.savebetter.core.domain.model.UserProfile
import com.example.savebetter.core.domain.model.WeeklyTarget
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import java.time.Instant

/**
 * Unit tests verifying bidirectional mapping between Room Entity ↔ Domain Model ↔ Firestore DTO.
 */
class DataMapperTest {

    private val testInstant = Instant.ofEpochMilli(1700000000000L)

    @Test
    fun `UserProfile roundtrip mapping preserves all fields`() {
        val domain = UserProfile(
            id = "user_123",
            name = "Ayesha Khan",
            email = "ayesha@example.com",
            monthlySalaryMinor = 6500000L,
            preferredLanguage = "bn",
            onboardingCompleted = true,
            createdAt = testInstant,
            updatedAt = testInstant,
            syncStatus = SyncState.PENDING
        )

        val entity = domain.toEntity()
        assertEquals(domain, entity.toDomain())

        val dto = domain.toFirestoreDto()
        val fromDto = dto.toDomain()
        assertEquals(domain.id, fromDto.id)
        assertEquals(domain.name, fromDto.name)
        assertEquals(domain.email, fromDto.email)
        assertEquals(domain.monthlySalaryMinor, fromDto.monthlySalaryMinor)
        assertEquals(domain.preferredLanguage, fromDto.preferredLanguage)
        assertEquals(domain.onboardingCompleted, fromDto.onboardingCompleted)
    }

    @Test
    fun `Expense roundtrip mapping preserves all fields`() {
        val domain = Expense(
            id = "exp_456",
            userId = "user_123",
            amountMinor = 142050L,
            categoryId = "cat_grocery",
            note = "Weekly groceries",
            date = testInstant,
            createdAt = testInstant,
            updatedAt = testInstant,
            syncStatus = SyncState.SYNCED,
            isDeleted = false,
            deletedAt = null
        )

        val entity = domain.toEntity()
        assertEquals(domain, entity.toDomain())

        val dto = domain.toFirestoreDto()
        val fromDto = dto.toDomain()
        assertEquals(domain.id, fromDto.id)
        assertEquals(domain.userId, fromDto.userId)
        assertEquals(domain.amountMinor, fromDto.amountMinor)
        assertEquals(domain.categoryId, fromDto.categoryId)
        assertEquals(domain.note, fromDto.note)
        assertEquals(domain.date, fromDto.date)
    }

    @Test
    fun `Category roundtrip mapping preserves default and custom categories`() {
        val defaultCat = Category(
            id = "cat_1",
            userId = "user_123",
            nameKey = "category_household",
            customName = null,
            icon = "home",
            colorToken = "cat2",
            isDefault = true,
            createdAt = testInstant,
            updatedAt = testInstant
        )

        assertEquals(defaultCat, defaultCat.toEntity().toDomain())
        val defaultDto = defaultCat.toFirestoreDto()
        assertEquals("category_household", defaultDto.toDomain().nameKey)

        val customCat = Category(
            id = "cat_2",
            userId = "user_123",
            nameKey = null,
            customName = "Freelance Subscriptions",
            icon = "star",
            colorToken = "cat4",
            isDefault = false,
            createdAt = testInstant,
            updatedAt = testInstant
        )

        assertEquals(customCat, customCat.toEntity().toDomain())
        val customDto = customCat.toFirestoreDto()
        assertEquals("Freelance Subscriptions", customDto.toDomain().customName)
    }

    @Test
    fun `Target roundtrip mapping preserves weekly and monthly targets`() {
        val weekly = WeeklyTarget(
            id = "wt_1",
            userId = "user_123",
            weekStart = "2026-09-07",
            weekEnd = "2026-09-13",
            targetAmountMinor = 1000000L,
            updatedAt = testInstant
        )
        assertEquals(weekly, weekly.toEntity().toDomain())
        assertEquals(weekly.targetAmountMinor, weekly.toFirestoreDto().toDomain().targetAmountMinor)

        val monthly = MonthlyTarget(
            id = "mt_1",
            userId = "user_123",
            month = 9,
            year = 2026,
            targetAmountMinor = 4000000L,
            savingGoalMinor = 1500000L,
            updatedAt = testInstant
        )
        assertEquals(monthly, monthly.toEntity().toDomain())
        assertEquals(monthly.savingGoalMinor, monthly.toFirestoreDto().toDomain().savingGoalMinor)

        val salaryRecord = SalaryHandRecord(
            id = "shr_1",
            userId = "user_123",
            month = 9,
            year = 2026,
            salaryAmountMinor = 6500000L,
            handRemainingAmountMinor = 2500000L,
            updatedAt = testInstant
        )
        assertEquals(salaryRecord, salaryRecord.toEntity().toDomain())
        assertEquals(salaryRecord.handRemainingAmountMinor, salaryRecord.toFirestoreDto().toDomain().handRemainingAmountMinor)
    }

    @Test
    fun `DebtCredit roundtrip mapping preserves receivable and payable items`() {
        val debt = DebtCredit(
            id = "dc_1",
            userId = "user_123",
            direction = DebtDirection.PAYABLE,
            personName = "Rafiq",
            amountMinor = 500000L,
            note = "Borrowed for office commute",
            date = testInstant,
            dueDate = testInstant.plusSeconds(86400 * 7),
            isSettled = false,
            createdAt = testInstant,
            updatedAt = testInstant,
            syncStatus = SyncState.PENDING
        )

        assertEquals(debt, debt.toEntity().toDomain())
        val dto = debt.toFirestoreDto()
        val fromDto = dto.toDomain()
        assertEquals(DebtDirection.PAYABLE, fromDto.direction)
        assertEquals("Rafiq", fromDto.personName)
        assertEquals(500000L, fromDto.amountMinor)
    }
}
