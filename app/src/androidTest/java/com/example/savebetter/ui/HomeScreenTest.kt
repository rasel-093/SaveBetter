package com.example.savebetter.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.savebetter.R
import com.example.savebetter.core.designsystem.theme.SaveBetterTheme
import com.example.savebetter.core.domain.model.MonthlyTarget
import com.example.savebetter.core.domain.model.UserProfile
import com.example.savebetter.core.domain.model.WeeklyTarget
import com.example.savebetter.core.domain.usecase.dashboard.GetMonthlySummaryUseCase
import com.example.savebetter.core.domain.usecase.dashboard.GetWeeklySummaryUseCase
import com.example.savebetter.core.domain.usecase.profile.GetUserProfileUseCase
import com.example.savebetter.feature.home.HomeViewModel
import com.example.savebetter.feature.home.ui.HomeScreen
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class HomeScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var userProfileRepository: FakeUserProfileRepository
    private lateinit var targetRepository: FakeTargetRepository
    private lateinit var expenseRepository: FakeExpenseRepository
    private lateinit var categoryRepository: FakeCategoryRepository
    private lateinit var viewModel: HomeViewModel

    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext

    @Before
    fun setUp() {
        val now = java.time.LocalDate.now()
        val weekStart = now.with(java.time.temporal.TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY)).toString()
        val weekEnd = now.with(java.time.temporal.TemporalAdjusters.nextOrSame(java.time.DayOfWeek.SUNDAY)).toString()

        userProfileRepository = FakeUserProfileRepository(
            UserProfile(id = "user_123", name = "Rahim", email = "rahim@example.com")
        )
        targetRepository = FakeTargetRepository(
            weeklyTargets = listOf(
                WeeklyTarget(
                    id = "wt_1",
                    userId = "user_123",
                    weekStart = weekStart,
                    weekEnd = weekEnd,
                    targetAmountMinor = 500000L
                )
            ),
            monthlyTargets = listOf(
                MonthlyTarget(
                    id = "mt_1",
                    userId = "user_123",
                    month = now.monthValue,
                    year = now.year,
                    targetAmountMinor = 2000000L
                )
            )
        )
        expenseRepository = FakeExpenseRepository()
        categoryRepository = FakeCategoryRepository()

        viewModel = HomeViewModel(
            getUserProfileUseCase = GetUserProfileUseCase(userProfileRepository),
            getWeeklySummaryUseCase = GetWeeklySummaryUseCase(targetRepository, expenseRepository),
            getMonthlySummaryUseCase = GetMonthlySummaryUseCase(targetRepository, expenseRepository),
            expenseRepository = expenseRepository,
            categoryRepository = categoryRepository
        )
    }

    @Test
    fun homeScreen_rendersDashboardCardsAndFab() {
        composeTestRule.setContent {
            SaveBetterTheme {
                HomeScreen(
                    userId = "user_123",
                    userName = "Rahim",
                    viewModel = viewModel
                )
            }
        }

        val weeklyTitle = context.getString(R.string.dashboard_weekly_title)
        val monthlyTitle = context.getString(R.string.dashboard_monthly_title)
        val fabDescription = context.getString(R.string.dashboard_fab_add_expense)

        composeTestRule.onNodeWithText(weeklyTitle, ignoreCase = true).assertIsDisplayed()
        composeTestRule.onNodeWithText(monthlyTitle, ignoreCase = true).assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription(fabDescription).assertIsDisplayed()
    }

    @Test
    fun homeScreen_clickingFab_triggersAddExpenseCallback() {
        var addExpenseClicked = false

        composeTestRule.setContent {
            SaveBetterTheme {
                HomeScreen(
                    userId = "user_123",
                    userName = "Rahim",
                    onAddExpenseClick = { addExpenseClicked = true },
                    viewModel = viewModel
                )
            }
        }

        val fabDescription = context.getString(R.string.dashboard_fab_add_expense)
        composeTestRule.onNodeWithContentDescription(fabDescription).performClick()

        assertTrue(addExpenseClicked)
    }
}
