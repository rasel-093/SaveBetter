package com.example.savebetter.ui

import com.example.savebetter.core.auth.model.AuthUser
import com.example.savebetter.core.auth.repository.AuthRepository
import com.example.savebetter.core.domain.model.Category
import com.example.savebetter.core.domain.model.DebtCredit
import com.example.savebetter.core.domain.model.Expense
import com.example.savebetter.core.domain.model.MonthlyTarget
import com.example.savebetter.core.domain.model.SalaryHandRecord
import com.example.savebetter.core.domain.model.UserProfile
import com.example.savebetter.core.domain.model.WeeklyTarget
import com.example.savebetter.core.domain.repository.CategoryRepository
import com.example.savebetter.core.domain.repository.DebtCreditRepository
import com.example.savebetter.core.domain.repository.ExpenseRepository
import com.example.savebetter.core.domain.repository.TargetRepository
import com.example.savebetter.core.domain.repository.UserProfileRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flowOf
import java.time.Instant

class FakeAuthRepository(
    initialUser: AuthUser? = null
) : AuthRepository {
    private val authState = MutableStateFlow(initialUser)
    var signOutCalled = false

    override fun observeAuthState(): Flow<AuthUser?> = authState.asStateFlow()

    override suspend fun signIn(email: String, password: String): Result<AuthUser> {
        val user = AuthUser(id = "user_123", email = email, displayName = "Test User")
        authState.value = user
        return Result.success(user)
    }

    override suspend fun signUp(email: String, password: String): Result<AuthUser> {
        val user = AuthUser(id = "user_123", email = email, displayName = "Test User")
        authState.value = user
        return Result.success(user)
    }

    override suspend fun signInWithGoogle(idToken: String): Result<AuthUser> {
        val user = AuthUser(id = "user_google", email = "google@example.com", displayName = "Google User")
        authState.value = user
        return Result.success(user)
    }

    override suspend fun sendPasswordResetEmail(email: String): Result<Unit> = Result.success(Unit)

    override suspend fun signOut() {
        signOutCalled = true
        authState.value = null
    }

    override suspend fun reauthenticate(password: String): Result<Unit> = Result.success(Unit)

    override suspend fun deleteAccount(): Result<Unit> {
        authState.value = null
        return Result.success(Unit)
    }
}

class FakeUserProfileRepository(
    private val profile: UserProfile? = UserProfile(id = "user_123", name = "Nafis Ahmed", email = "nafis@example.com")
) : UserProfileRepository {
    override fun observeUserProfile(userId: String): Flow<UserProfile?> = flowOf(profile)
    override suspend fun getUserProfile(userId: String): UserProfile? = profile
    override suspend fun saveUserProfile(profile: UserProfile) {}
    override suspend fun updateOnboardingCompleted(userId: String, completed: Boolean) {}
    override suspend fun syncUserProfile(userId: String): Result<Unit> = Result.success(Unit)
    override suspend fun deleteUserData(userId: String): Result<Unit> = Result.success(Unit)
}

class FakeCategoryRepository(
    private val categories: List<Category> = emptyList()
) : CategoryRepository {
    override fun observeCategories(userId: String): Flow<List<Category>> = flowOf(categories)
    override suspend fun getCategoryById(id: String): Category? = categories.find { it.id == id }
    override suspend fun addCategory(category: Category) {}
    override suspend fun initializeDefaultCategories(userId: String) {}
    override suspend fun deleteCategory(id: String) {}
    override suspend fun syncPendingCategories(userId: String): Result<Unit> = Result.success(Unit)
}

class FakeExpenseRepository(
    private val expenses: List<Expense> = emptyList()
) : ExpenseRepository {
    override fun observeExpenses(userId: String): Flow<List<Expense>> = flowOf(expenses)
    override fun observeExpensesByDateRange(userId: String, startDate: Instant, endDate: Instant): Flow<List<Expense>> = flowOf(expenses)
    override suspend fun getExpenseById(id: String): Expense? = expenses.find { it.id == id }
    override suspend fun addExpense(expense: Expense) {}
    override suspend fun updateExpense(expense: Expense) {}
    override suspend fun deleteExpense(id: String) {}
    override suspend fun syncPendingExpenses(userId: String): Result<Unit> = Result.success(Unit)
}

class FakeTargetRepository(
    private val weeklyTargets: List<WeeklyTarget> = emptyList(),
    private val monthlyTargets: List<MonthlyTarget> = emptyList(),
    private val salaryRecords: List<SalaryHandRecord> = emptyList()
) : TargetRepository {
    override fun observeWeeklyTargets(userId: String): Flow<List<WeeklyTarget>> = flowOf(weeklyTargets)
    override suspend fun getWeeklyTargetForDate(userId: String, date: String): WeeklyTarget? = weeklyTargets.find { it.weekStart == date }
    override suspend fun saveWeeklyTarget(target: WeeklyTarget) {}

    override fun observeMonthlyTargets(userId: String): Flow<List<MonthlyTarget>> = flowOf(monthlyTargets)
    override suspend fun getMonthlyTarget(userId: String, year: Int, month: Int): MonthlyTarget? = monthlyTargets.find { it.year == year && it.month == month }
    override suspend fun saveMonthlyTarget(target: MonthlyTarget) {}

    override fun observeSalaryHandRecords(userId: String): Flow<List<SalaryHandRecord>> = flowOf(salaryRecords)
    override suspend fun getSalaryHandRecord(userId: String, year: Int, month: Int): SalaryHandRecord? = salaryRecords.find { it.year == year && it.month == month }
    override suspend fun saveSalaryHandRecord(record: SalaryHandRecord) {}

    override suspend fun syncPendingTargets(userId: String): Result<Unit> = Result.success(Unit)
}

class FakeDebtCreditRepository : DebtCreditRepository {
    override fun observeDebtsAndCredits(userId: String): Flow<List<DebtCredit>> = flowOf(emptyList())
    override suspend fun getDebtCreditById(id: String): DebtCredit? = null
    override suspend fun addDebtCredit(item: DebtCredit) {}
    override suspend fun updateDebtCredit(item: DebtCredit) {}
    override suspend fun markSettled(id: String, isSettled: Boolean) {}
    override suspend fun deleteDebtCredit(id: String) {}
    override suspend fun syncPendingDebtsAndCredits(userId: String): Result<Unit> = Result.success(Unit)
}
