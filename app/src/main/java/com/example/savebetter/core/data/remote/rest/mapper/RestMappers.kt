package com.example.savebetter.core.data.remote.rest.mapper

import com.example.savebetter.core.auth.model.AuthUser
import com.example.savebetter.core.auth.model.UserId
import com.example.savebetter.core.data.remote.rest.dto.RestAuthResponse
import com.example.savebetter.core.data.remote.rest.dto.RestCategoryDto
import com.example.savebetter.core.data.remote.rest.dto.RestDebtCreditDto
import com.example.savebetter.core.data.remote.rest.dto.RestExpenseDto
import com.example.savebetter.core.data.remote.rest.dto.RestMonthlyTargetDto
import com.example.savebetter.core.data.remote.rest.dto.RestSalaryHandRecordDto
import com.example.savebetter.core.data.remote.rest.dto.RestUserProfileDto
import com.example.savebetter.core.data.remote.rest.dto.RestWeeklyTargetDto
import com.example.savebetter.core.domain.model.Category
import com.example.savebetter.core.domain.model.DebtCredit
import com.example.savebetter.core.domain.model.DebtDirection
import com.example.savebetter.core.domain.model.Expense
import com.example.savebetter.core.domain.model.MonthlyTarget
import com.example.savebetter.core.domain.model.SalaryHandRecord
import com.example.savebetter.core.domain.model.SyncState
import com.example.savebetter.core.domain.model.UserProfile
import com.example.savebetter.core.domain.model.WeeklyTarget
import java.time.Instant

/**
 * Bidirectional mappers between stack-agnostic REST DTOs and Domain models.
 *
 * Notice: These mappers know nothing about Firebase, Room, or UI.
 * They keep the domain layer completely insulated from network representations.
 */

// ── Authentication ───────────────────────────────────────────────────────────

fun RestAuthResponse.toDomain(): AuthUser = AuthUser(
    id = userId,
    email = email,
    displayName = displayName
)

// ── User Profile ─────────────────────────────────────────────────────────────

fun RestUserProfileDto.toDomain(): UserProfile = UserProfile(
    id = id,
    name = name,
    email = email,
    monthlySalaryMinor = monthlySalaryMinor,
    preferredLanguage = preferredLanguage,
    onboardingCompleted = onboardingCompleted,
    createdAt = parseInstantSafely(createdAt),
    updatedAt = parseInstantSafely(updatedAt),
    syncStatus = SyncState.SYNCED
)

fun UserProfile.toRestDto(): RestUserProfileDto = RestUserProfileDto(
    id = id,
    name = name,
    email = email,
    monthlySalaryMinor = monthlySalaryMinor,
    preferredLanguage = preferredLanguage,
    onboardingCompleted = onboardingCompleted,
    createdAt = createdAt.toString(),
    updatedAt = updatedAt.toString()
)

// ── Expenses ─────────────────────────────────────────────────────────────────

fun RestExpenseDto.toDomain(): Expense = Expense(
    id = id,
    userId = userId,
    amountMinor = amountMinor,
    categoryId = categoryId,
    note = note,
    date = parseInstantSafely(date),
    createdAt = parseInstantSafely(createdAt),
    updatedAt = parseInstantSafely(updatedAt),
    syncStatus = SyncState.SYNCED,
    isDeleted = isDeleted,
    deletedAt = deletedAt?.let { parseInstantSafely(it) }
)

fun Expense.toRestDto(): RestExpenseDto = RestExpenseDto(
    id = id,
    userId = userId,
    amountMinor = amountMinor,
    categoryId = categoryId,
    note = note,
    date = date.toString(),
    createdAt = createdAt.toString(),
    updatedAt = updatedAt.toString(),
    isDeleted = isDeleted,
    deletedAt = deletedAt?.toString()
)

// ── Categories ───────────────────────────────────────────────────────────────

fun RestCategoryDto.toDomain(): Category = Category(
    id = id,
    userId = userId,
    nameKey = nameKey,
    customName = customName,
    icon = icon,
    colorToken = colorToken,
    isDefault = isDefault,
    createdAt = parseInstantSafely(createdAt),
    updatedAt = parseInstantSafely(updatedAt),
    syncStatus = SyncState.SYNCED,
    isDeleted = isDeleted
)

fun Category.toRestDto(): RestCategoryDto = RestCategoryDto(
    id = id,
    userId = userId,
    nameKey = nameKey,
    customName = customName,
    icon = icon,
    colorToken = colorToken,
    isDefault = isDefault,
    createdAt = createdAt.toString(),
    updatedAt = updatedAt.toString(),
    isDeleted = isDeleted
)

// ── Targets & Salary Records ─────────────────────────────────────────────────

fun RestWeeklyTargetDto.toDomain(): WeeklyTarget = WeeklyTarget(
    id = id,
    userId = userId,
    weekStart = weekStart,
    weekEnd = weekEnd,
    targetAmountMinor = targetAmountMinor,
    updatedAt = parseInstantSafely(updatedAt),
    syncStatus = SyncState.SYNCED
)

fun WeeklyTarget.toRestDto(): RestWeeklyTargetDto = RestWeeklyTargetDto(
    id = id,
    userId = userId,
    weekStart = weekStart,
    weekEnd = weekEnd,
    targetAmountMinor = targetAmountMinor,
    updatedAt = updatedAt.toString()
)

fun RestMonthlyTargetDto.toDomain(): MonthlyTarget = MonthlyTarget(
    id = id,
    userId = userId,
    month = month,
    year = year,
    targetAmountMinor = targetAmountMinor,
    savingGoalMinor = savingGoalMinor,
    updatedAt = parseInstantSafely(updatedAt),
    syncStatus = SyncState.SYNCED
)

fun MonthlyTarget.toRestDto(): RestMonthlyTargetDto = RestMonthlyTargetDto(
    id = id,
    userId = userId,
    month = month,
    year = year,
    targetAmountMinor = targetAmountMinor,
    savingGoalMinor = savingGoalMinor,
    updatedAt = updatedAt.toString()
)

fun RestSalaryHandRecordDto.toDomain(): SalaryHandRecord = SalaryHandRecord(
    id = id,
    userId = userId,
    month = month,
    year = year,
    salaryAmountMinor = salaryAmountMinor,
    handRemainingAmountMinor = handRemainingAmountMinor,
    updatedAt = parseInstantSafely(updatedAt),
    syncStatus = SyncState.SYNCED
)

fun SalaryHandRecord.toRestDto(): RestSalaryHandRecordDto = RestSalaryHandRecordDto(
    id = id,
    userId = userId,
    month = month,
    year = year,
    salaryAmountMinor = salaryAmountMinor,
    handRemainingAmountMinor = handRemainingAmountMinor,
    updatedAt = updatedAt.toString()
)

// ── Debts & Credits ──────────────────────────────────────────────────────────

fun RestDebtCreditDto.toDomain(): DebtCredit = DebtCredit(
    id = id,
    userId = userId,
    direction = runCatching { DebtDirection.valueOf(direction) }.getOrDefault(DebtDirection.PAYABLE),
    personName = personName,
    amountMinor = amountMinor,
    note = note,
    date = parseInstantSafely(date),
    dueDate = dueDate?.let { parseInstantSafely(it) },
    isSettled = isSettled,
    createdAt = parseInstantSafely(createdAt),
    updatedAt = parseInstantSafely(updatedAt),
    syncStatus = SyncState.SYNCED,
    isDeleted = isDeleted,
    deletedAt = deletedAt?.let { parseInstantSafely(it) }
)

fun DebtCredit.toRestDto(): RestDebtCreditDto = RestDebtCreditDto(
    id = id,
    userId = userId,
    direction = direction.name,
    personName = personName,
    amountMinor = amountMinor,
    note = note,
    date = date.toString(),
    dueDate = dueDate?.toString(),
    isSettled = isSettled,
    createdAt = createdAt.toString(),
    updatedAt = updatedAt.toString(),
    isDeleted = isDeleted,
    deletedAt = deletedAt?.toString()
)

// ── Helpers ──────────────────────────────────────────────────────────────────

private fun parseInstantSafely(value: String): Instant = runCatching {
    Instant.parse(value)
}.getOrElse {
    // Fallback if milliseconds or slight ISO deviation is returned by custom backend
    runCatching {
        val epochMilli = value.toLong()
        Instant.ofEpochMilli(epochMilli)
    }.getOrDefault(Instant.now())
}
