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

/**
 * Standard RESTful API contract for SaveBetter backend.
 *
 * This contract is completely stack-agnostic and can be implemented by:
 * - Django REST Framework (Python)
 * - FastAPI (Python)
 * - Spring Boot (Java/Kotlin)
 * - Express / NestJS (Node.js / TypeScript)
 * - Gin / Fiber (Go)
 * - ASP.NET Core (C#)
 */
interface SaveBetterRestApi {

    // ── Authentication ───────────────────────────────────────────────────────
    // POST /api/v1/auth/login/
    suspend fun login(request: RestLoginRequest): Result<RestAuthResponse>

    // POST /api/v1/auth/register/
    suspend fun register(request: RestRegisterRequest): Result<RestAuthResponse>

    // POST /api/v1/auth/google/
    suspend fun signInWithGoogle(request: RestGoogleSignInRequest): Result<RestAuthResponse>

    // POST /api/v1/auth/password-reset/
    suspend fun sendPasswordResetEmail(request: RestPasswordResetRequest): Result<Unit>

    // POST /api/v1/auth/refresh/
    suspend fun refreshToken(request: RestRefreshTokenRequest): Result<RestAuthResponse>

    // POST /api/v1/auth/logout/
    suspend fun logout(): Result<Unit>

    // DELETE /api/v1/auth/account/
    suspend fun deleteAccount(): Result<Unit>

    // ── User Profile ─────────────────────────────────────────────────────────
    // GET /api/v1/users/{userId}/profile/
    suspend fun getUserProfile(userId: String): Result<RestUserProfileDto?>

    // PUT /api/v1/users/{userId}/profile/
    suspend fun saveUserProfile(userId: String, profile: RestUserProfileDto): Result<Unit>

    // DELETE /api/v1/users/{userId}/data/
    suspend fun deleteUserData(userId: String): Result<Unit>

    // ── Expenses ─────────────────────────────────────────────────────────────
    // GET /api/v1/users/{userId}/expenses/
    suspend fun fetchExpenses(userId: String): Result<List<RestExpenseDto>>

    // POST /api/v1/users/{userId}/expenses/
    suspend fun uploadExpense(userId: String, expense: RestExpenseDto): Result<Unit>

    // DELETE /api/v1/users/{userId}/expenses/{expenseId}/
    suspend fun deleteExpense(userId: String, expenseId: String): Result<Unit>

    // ── Categories ───────────────────────────────────────────────────────────
    // GET /api/v1/users/{userId}/categories/
    suspend fun fetchCategories(userId: String): Result<List<RestCategoryDto>>

    // POST /api/v1/users/{userId}/categories/
    suspend fun uploadCategory(userId: String, category: RestCategoryDto): Result<Unit>

    // DELETE /api/v1/users/{userId}/categories/{categoryId}/
    suspend fun deleteCategory(userId: String, categoryId: String): Result<Unit>

    // ── Targets & Salary Hand Records ────────────────────────────────────────
    // GET /api/v1/users/{userId}/targets/weekly/
    suspend fun fetchWeeklyTargets(userId: String): Result<List<RestWeeklyTargetDto>>

    // POST /api/v1/users/{userId}/targets/weekly/
    suspend fun uploadWeeklyTarget(userId: String, target: RestWeeklyTargetDto): Result<Unit>

    // GET /api/v1/users/{userId}/targets/monthly/
    suspend fun fetchMonthlyTargets(userId: String): Result<List<RestMonthlyTargetDto>>

    // POST /api/v1/users/{userId}/targets/monthly/
    suspend fun uploadMonthlyTarget(userId: String, target: RestMonthlyTargetDto): Result<Unit>

    // GET /api/v1/users/{userId}/salary-records/
    suspend fun fetchSalaryHandRecords(userId: String): Result<List<RestSalaryHandRecordDto>>

    // POST /api/v1/users/{userId}/salary-records/
    suspend fun uploadSalaryHandRecord(userId: String, record: RestSalaryHandRecordDto): Result<Unit>

    // ── Debts & Credits ──────────────────────────────────────────────────────
    // GET /api/v1/users/{userId}/debts-credits/
    suspend fun fetchDebtsAndCredits(userId: String): Result<List<RestDebtCreditDto>>

    // POST /api/v1/users/{userId}/debts-credits/
    suspend fun uploadDebtCredit(userId: String, item: RestDebtCreditDto): Result<Unit>

    // DELETE /api/v1/users/{userId}/debts-credits/{id}/
    suspend fun deleteDebtCredit(userId: String, id: String): Result<Unit>
}
