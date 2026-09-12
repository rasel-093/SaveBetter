package com.example.savebetter.core.data.remote.rest.api

import com.example.savebetter.core.data.remote.rest.dto.RestAuthResponse
import com.example.savebetter.core.data.remote.rest.dto.RestCategoryDto
import com.example.savebetter.core.data.remote.rest.dto.RestDebtCreditDto
import com.example.savebetter.core.data.remote.rest.dto.RestExpenseDto
import com.example.savebetter.core.data.remote.rest.dto.RestGoogleSignInRequest
import com.example.savebetter.core.data.remote.rest.dto.RestLoginRequest
import com.example.savebetter.core.data.remote.rest.dto.RestMonthlyTargetDto
import com.example.savebetter.core.data.remote.rest.dto.RestPasswordResetRequest
import com.example.savebetter.core.data.remote.rest.dto.RestRefreshTokenRequest
import com.example.savebetter.core.data.remote.rest.dto.RestRegisterRequest
import com.example.savebetter.core.data.remote.rest.dto.RestSalaryHandRecordDto
import com.example.savebetter.core.data.remote.rest.dto.RestUserProfileDto
import com.example.savebetter.core.data.remote.rest.dto.RestWeeklyTargetDto
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Reference in-memory implementation of [SaveBetterRestApi].
 *
 * Demonstrates a fully working RESTful contract client that can be verified and tested
 * offline or used as a template when plugging in Retrofit or Ktor HTTP clients.
 */
@Singleton
class MockSaveBetterRestApi @Inject constructor() : SaveBetterRestApi {

    private val users = ConcurrentHashMap<String, RestUserProfileDto>()
    private val expenses = ConcurrentHashMap<String, MutableMap<String, RestExpenseDto>>()
    private val categories = ConcurrentHashMap<String, MutableMap<String, RestCategoryDto>>()
    private val weeklyTargets = ConcurrentHashMap<String, MutableMap<String, RestWeeklyTargetDto>>()
    private val monthlyTargets = ConcurrentHashMap<String, MutableMap<String, RestMonthlyTargetDto>>()
    private val salaryRecords = ConcurrentHashMap<String, MutableMap<String, RestSalaryHandRecordDto>>()
    private val debtCredits = ConcurrentHashMap<String, MutableMap<String, RestDebtCreditDto>>()

    override suspend fun login(request: RestLoginRequest): Result<RestAuthResponse> = runCatching {
        val simulatedUid = "rest_user_${request.email.hashCode().toUInt()}"
        RestAuthResponse(
            userId = simulatedUid,
            email = request.email,
            displayName = request.email.substringBefore("@"),
            accessToken = "jwt_access_token_${UUID.randomUUID()}",
            refreshToken = "jwt_refresh_token_${UUID.randomUUID()}"
        )
    }

    override suspend fun register(request: RestRegisterRequest): Result<RestAuthResponse> = runCatching {
        val simulatedUid = "rest_user_${request.email.hashCode().toUInt()}"
        RestAuthResponse(
            userId = simulatedUid,
            email = request.email,
            displayName = request.displayName ?: request.email.substringBefore("@"),
            accessToken = "jwt_access_token_${UUID.randomUUID()}",
            refreshToken = "jwt_refresh_token_${UUID.randomUUID()}"
        )
    }

    override suspend fun signInWithGoogle(request: RestGoogleSignInRequest): Result<RestAuthResponse> = runCatching {
        val simulatedUid = "rest_google_user_${request.idToken.hashCode().toUInt()}"
        RestAuthResponse(
            userId = simulatedUid,
            email = "google_user@example.com",
            displayName = "Google User",
            accessToken = "jwt_google_access_${UUID.randomUUID()}"
        )
    }

    override suspend fun sendPasswordResetEmail(request: RestPasswordResetRequest): Result<Unit> = runCatching {
        Unit
    }

    override suspend fun refreshToken(request: RestRefreshTokenRequest): Result<RestAuthResponse> = runCatching {
        RestAuthResponse(
            userId = "refreshed_user",
            email = "user@example.com",
            displayName = "User",
            accessToken = "jwt_refreshed_access_${UUID.randomUUID()}"
        )
    }

    override suspend fun logout(): Result<Unit> = runCatching { Unit }

    override suspend fun deleteAccount(): Result<Unit> = runCatching { Unit }

    override suspend fun getUserProfile(userId: String): Result<RestUserProfileDto?> = runCatching {
        users[userId]
    }

    override suspend fun saveUserProfile(userId: String, profile: RestUserProfileDto): Result<Unit> = runCatching {
        users[userId] = profile
    }

    override suspend fun deleteUserData(userId: String): Result<Unit> = runCatching {
        users.remove(userId)
        expenses.remove(userId)
        categories.remove(userId)
        weeklyTargets.remove(userId)
        monthlyTargets.remove(userId)
        salaryRecords.remove(userId)
        debtCredits.remove(userId)
    }

    override suspend fun fetchExpenses(userId: String): Result<List<RestExpenseDto>> = runCatching {
        expenses[userId]?.values?.toList() ?: emptyList()
    }

    override suspend fun uploadExpense(userId: String, expense: RestExpenseDto): Result<Unit> = runCatching {
        val userMap = expenses.getOrPut(userId) { ConcurrentHashMap() }
        userMap[expense.id] = expense
    }

    override suspend fun deleteExpense(userId: String, expenseId: String): Result<Unit> = runCatching {
        expenses[userId]?.remove(expenseId)
    }

    override suspend fun fetchCategories(userId: String): Result<List<RestCategoryDto>> = runCatching {
        categories[userId]?.values?.toList() ?: emptyList()
    }

    override suspend fun uploadCategory(userId: String, category: RestCategoryDto): Result<Unit> = runCatching {
        val userMap = categories.getOrPut(userId) { ConcurrentHashMap() }
        userMap[category.id] = category
    }

    override suspend fun deleteCategory(userId: String, categoryId: String): Result<Unit> = runCatching {
        categories[userId]?.remove(categoryId)
    }

    override suspend fun fetchWeeklyTargets(userId: String): Result<List<RestWeeklyTargetDto>> = runCatching {
        weeklyTargets[userId]?.values?.toList() ?: emptyList()
    }

    override suspend fun uploadWeeklyTarget(userId: String, target: RestWeeklyTargetDto): Result<Unit> = runCatching {
        val userMap = weeklyTargets.getOrPut(userId) { ConcurrentHashMap() }
        userMap[target.id] = target
    }

    override suspend fun fetchMonthlyTargets(userId: String): Result<List<RestMonthlyTargetDto>> = runCatching {
        monthlyTargets[userId]?.values?.toList() ?: emptyList()
    }

    override suspend fun uploadMonthlyTarget(userId: String, target: RestMonthlyTargetDto): Result<Unit> = runCatching {
        val userMap = monthlyTargets.getOrPut(userId) { ConcurrentHashMap() }
        userMap[target.id] = target
    }

    override suspend fun fetchSalaryHandRecords(userId: String): Result<List<RestSalaryHandRecordDto>> = runCatching {
        salaryRecords[userId]?.values?.toList() ?: emptyList()
    }

    override suspend fun uploadSalaryHandRecord(userId: String, record: RestSalaryHandRecordDto): Result<Unit> = runCatching {
        val userMap = salaryRecords.getOrPut(userId) { ConcurrentHashMap() }
        userMap[record.id] = record
    }

    override suspend fun fetchDebtsAndCredits(userId: String): Result<List<RestDebtCreditDto>> = runCatching {
        debtCredits[userId]?.values?.toList() ?: emptyList()
    }

    override suspend fun uploadDebtCredit(userId: String, item: RestDebtCreditDto): Result<Unit> = runCatching {
        val userMap = debtCredits.getOrPut(userId) { ConcurrentHashMap() }
        userMap[item.id] = item
    }

    override suspend fun deleteDebtCredit(userId: String, id: String): Result<Unit> = runCatching {
        debtCredits[userId]?.remove(id)
    }
}
