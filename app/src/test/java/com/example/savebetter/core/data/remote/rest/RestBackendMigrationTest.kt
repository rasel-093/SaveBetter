package com.example.savebetter.core.data.remote.rest

import com.example.savebetter.core.data.remote.rest.api.MockSaveBetterRestApi
import com.example.savebetter.core.data.remote.rest.datasource.RestAuthRemoteDataSource
import com.example.savebetter.core.data.remote.rest.datasource.RestCategoryRemoteDataSource
import com.example.savebetter.core.data.remote.rest.datasource.RestDebtCreditRemoteDataSource
import com.example.savebetter.core.data.remote.rest.datasource.RestExpenseRemoteDataSource
import com.example.savebetter.core.data.remote.rest.datasource.RestTargetRemoteDataSource
import com.example.savebetter.core.data.remote.rest.datasource.RestUserProfileRemoteDataSource
import com.example.savebetter.core.data.remote.rest.dto.RestExpenseDto
import com.example.savebetter.core.data.remote.rest.mapper.toDomain
import com.example.savebetter.core.data.remote.rest.mapper.toRestDto
import com.example.savebetter.core.domain.model.Category
import com.example.savebetter.core.domain.model.DebtCredit
import com.example.savebetter.core.domain.model.DebtDirection
import com.example.savebetter.core.domain.model.Expense
import com.example.savebetter.core.domain.model.MonthlyTarget
import com.example.savebetter.core.domain.model.SalaryHandRecord
import com.example.savebetter.core.domain.model.UserProfile
import com.example.savebetter.core.domain.model.WeeklyTarget
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.Instant

/**
 * Step 16 Verification Test:
 * Proves that SaveBetter's domain model, entities, and repository contracts
 * seamlessly operate with any stack-agnostic RESTful API backend.
 */
class RestBackendMigrationTest {

    private lateinit var mockApi: MockSaveBetterRestApi
    private lateinit var authDataSource: RestAuthRemoteDataSource
    private lateinit var expenseDataSource: RestExpenseRemoteDataSource
    private lateinit var categoryDataSource: RestCategoryRemoteDataSource
    private lateinit var targetDataSource: RestTargetRemoteDataSource
    private lateinit var debtCreditDataSource: RestDebtCreditRemoteDataSource
    private lateinit var userProfileDataSource: RestUserProfileRemoteDataSource

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    @Before
    fun setUp() {
        mockApi = MockSaveBetterRestApi()
        authDataSource = RestAuthRemoteDataSource(mockApi)
        expenseDataSource = RestExpenseRemoteDataSource(mockApi)
        categoryDataSource = RestCategoryRemoteDataSource(mockApi)
        targetDataSource = RestTargetRemoteDataSource(mockApi)
        debtCreditDataSource = RestDebtCreditRemoteDataSource(mockApi)
        userProfileDataSource = RestUserProfileRemoteDataSource(mockApi)
    }

    @Test
    fun restAuth_signInAndSignOut_updatesAuthStateFlow() = runTest {
        val initialUser = authDataSource.observeAuthState().first()
        assertNull(initialUser)

        val signInResult = authDataSource.signIn("test@example.com", "SecurePass123!")
        assertTrue(signInResult.isSuccess)
        val user = signInResult.getOrNull()
        assertNotNull(user)
        assertEquals("test@example.com", user?.email)

        val currentUser = authDataSource.observeAuthState().first()
        assertEquals(user?.id, currentUser?.id)

        authDataSource.signOut()
        val afterSignOutUser = authDataSource.observeAuthState().first()
        assertNull(afterSignOutUser)
    }

    @Test
    fun restExpense_uploadAndFetch_matchesDomainModelAccurately() = runTest {
        val now = Instant.now()
        val expense = Expense(
            id = "exp-rest-001",
            userId = "user-123",
            amountMinor = 35000L, // 350.00 BDT
            categoryId = "cat-food",
            note = "Grocery shopping",
            date = now,
            createdAt = now,
            updatedAt = now
        )

        val uploadResult = expenseDataSource.uploadExpense(expense)
        assertTrue(uploadResult.isSuccess)

        val fetchResult = expenseDataSource.fetchExpenses("user-123")
        assertTrue(fetchResult.isSuccess)
        val fetchedList = fetchResult.getOrThrow()
        assertEquals(1, fetchedList.size)

        val fetched = fetchedList[0]
        assertEquals("exp-rest-001", fetched.id)
        assertEquals("user-123", fetched.userId)
        assertEquals(35000L, fetched.amountMinor)
        assertEquals("cat-food", fetched.categoryId)
        assertEquals("Grocery shopping", fetched.note)
    }

    @Test
    fun restCategory_uploadAndFetch_supportsDefaultAndCustomCategories() = runTest {
        val customCat = Category(
            id = "cat-custom-1",
            userId = "user-123",
            nameKey = null,
            customName = "Freelance Gear",
            icon = "laptop",
            colorToken = "cat3",
            isDefault = false
        )

        assertTrue(categoryDataSource.uploadCategory(customCat).isSuccess)

        val fetched = categoryDataSource.fetchCategories("user-123").getOrThrow()
        assertEquals(1, fetched.size)
        assertEquals("Freelance Gear", fetched[0].customName)
        assertEquals("laptop", fetched[0].icon)
    }

    @Test
    fun restTargets_weeklyMonthlyAndSalary_roundtripAccurately() = runTest {
        val weeklyTarget = WeeklyTarget(
            id = "wt-1",
            userId = "user-123",
            weekStart = "2026-09-07",
            weekEnd = "2026-09-13",
            targetAmountMinor = 500000L
        )
        val monthlyTarget = MonthlyTarget(
            id = "mt-1",
            userId = "user-123",
            month = 9,
            year = 2026,
            targetAmountMinor = 2000000L,
            savingGoalMinor = 500000L
        )
        val salaryRecord = SalaryHandRecord(
            id = "sr-1",
            userId = "user-123",
            month = 9,
            year = 2026,
            salaryAmountMinor = 3000000L,
            handRemainingAmountMinor = 800000L
        )

        assertTrue(targetDataSource.uploadWeeklyTarget(weeklyTarget).isSuccess)
        assertTrue(targetDataSource.uploadMonthlyTarget(monthlyTarget).isSuccess)
        assertTrue(targetDataSource.uploadSalaryHandRecord(salaryRecord).isSuccess)

        val weeklyList = targetDataSource.fetchWeeklyTargets("user-123").getOrThrow()
        val monthlyList = targetDataSource.fetchMonthlyTargets("user-123").getOrThrow()
        val salaryList = targetDataSource.fetchSalaryHandRecords("user-123").getOrThrow()

        assertEquals(1, weeklyList.size)
        assertEquals(500000L, weeklyList[0].targetAmountMinor)
        assertEquals(1, monthlyList.size)
        assertEquals(2000000L, monthlyList[0].targetAmountMinor)
        assertEquals(1, salaryList.size)
        assertEquals(3000000L, salaryList[0].salaryAmountMinor)
    }

    @Test
    fun restDebtCredit_supportsReceivableAndPayableDirections() = runTest {
        val receivable = DebtCredit(
            id = "dc-1",
            userId = "user-123",
            direction = DebtDirection.RECEIVABLE,
            personName = "Kamal",
            amountMinor = 120000L,
            date = Instant.now()
        )
        val payable = DebtCredit(
            id = "dc-2",
            userId = "user-123",
            direction = DebtDirection.PAYABLE,
            personName = "Jamal",
            amountMinor = 80000L,
            date = Instant.now()
        )

        assertTrue(debtCreditDataSource.uploadDebtCredit(receivable).isSuccess)
        assertTrue(debtCreditDataSource.uploadDebtCredit(payable).isSuccess)

        val items = debtCreditDataSource.fetchDebtsAndCredits("user-123").getOrThrow()
        assertEquals(2, items.size)
        assertTrue(items.any { it.direction == DebtDirection.RECEIVABLE && it.personName == "Kamal" })
        assertTrue(items.any { it.direction == DebtDirection.PAYABLE && it.personName == "Jamal" })
    }

    @Test
    fun restUserProfile_savesAndRetrievesProfile() = runTest {
        val profile = UserProfile(
            id = "user-123",
            name = "Test User",
            email = "test@example.com",
            monthlySalaryMinor = 5000000L,
            preferredLanguage = "bn",
            onboardingCompleted = true
        )

        assertTrue(userProfileDataSource.saveUserProfile(profile).isSuccess)

        val retrieved = userProfileDataSource.fetchUserProfile("user-123").getOrThrow()
        assertNotNull(retrieved)
        assertEquals("Test User", retrieved?.name)
        assertEquals("bn", retrieved?.preferredLanguage)
        assertTrue(retrieved?.onboardingCompleted == true)
    }

    @Test
    fun jsonSerialization_encodesAndDecodesRestExpenseDtoStandardJson() {
        val dto = RestExpenseDto(
            id = "exp-json-01",
            userId = "user-99",
            amountMinor = 150000L,
            categoryId = "cat-transport",
            note = "Bus ticket",
            date = "2026-09-12T10:00:00Z",
            createdAt = "2026-09-12T10:00:00Z",
            updatedAt = "2026-09-12T10:00:00Z",
            isDeleted = false
        )

        val jsonString = json.encodeToString(dto)
        assertTrue(jsonString.contains("\"amount_minor\":150000"))
        assertTrue(jsonString.contains("\"category_id\":\"cat-transport\""))

        val decoded = json.decodeFromString<RestExpenseDto>(jsonString)
        assertEquals(dto.id, decoded.id)
        assertEquals(dto.amountMinor, decoded.amountMinor)
    }
}
