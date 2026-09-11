package com.example.savebetter.feature.auth

import app.cash.turbine.test
import com.example.savebetter.core.auth.model.AuthUser
import com.example.savebetter.core.domain.model.UserProfile
import com.example.savebetter.core.domain.usecase.auth.ObserveAuthStateUseCase
import com.example.savebetter.core.domain.usecase.profile.GetUserProfileUseCase
import com.example.savebetter.util.MainDispatcherRule
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class AuthGateViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val observeAuthStateUseCase: ObserveAuthStateUseCase = mockk()
    private val getUserProfileUseCase: GetUserProfileUseCase = mockk()

    private val testUser = AuthUser(
        id = "user_456",
        email = "persisted@example.com",
        displayName = "Persisted User"
    )

    private val completedProfile = UserProfile(
        id = "user_456",
        name = "Persisted User",
        email = "persisted@example.com",
        monthlySalaryMinor = 5000000L,
        onboardingCompleted = true
    )

    private val pendingProfile = UserProfile(
        id = "user_456",
        name = "Persisted User",
        email = "persisted@example.com",
        monthlySalaryMinor = 0L,
        onboardingCompleted = false
    )

    @Test
    fun `gateState emits Unauthenticated when user is null`() = runTest {
        every { observeAuthStateUseCase() } returns flowOf(null)

        val viewModel = AuthGateViewModel(observeAuthStateUseCase, getUserProfileUseCase)

        viewModel.gateState.test {
            val item = awaitItem()
            if (item is AuthGateState.Loading) {
                val next = awaitItem()
                assertEquals(AuthGateState.Unauthenticated, next)
            } else {
                assertEquals(AuthGateState.Unauthenticated, item)
            }
        }
    }

    @Test
    fun `gateState emits NeedsOnboarding when user is logged in but profile is null`() = runTest {
        every { observeAuthStateUseCase() } returns flowOf(testUser)
        every { getUserProfileUseCase("user_456") } returns flowOf(null)

        val viewModel = AuthGateViewModel(observeAuthStateUseCase, getUserProfileUseCase)

        viewModel.gateState.test {
            val item = awaitItem()
            val finalState = if (item is AuthGateState.Loading) awaitItem() else item
            assertTrue(finalState is AuthGateState.NeedsOnboarding)
            assertEquals(testUser, (finalState as AuthGateState.NeedsOnboarding).user)
        }
    }

    @Test
    fun `gateState emits NeedsOnboarding when user is logged in and onboardingCompleted is false`() = runTest {
        every { observeAuthStateUseCase() } returns flowOf(testUser)
        every { getUserProfileUseCase("user_456") } returns flowOf(pendingProfile)

        val viewModel = AuthGateViewModel(observeAuthStateUseCase, getUserProfileUseCase)

        viewModel.gateState.test {
            val item = awaitItem()
            val finalState = if (item is AuthGateState.Loading) awaitItem() else item
            assertTrue(finalState is AuthGateState.NeedsOnboarding)
            assertEquals(testUser, (finalState as AuthGateState.NeedsOnboarding).user)
        }
    }

    @Test
    fun `gateState emits Authenticated when user is logged in and onboardingCompleted is true`() = runTest {
        every { observeAuthStateUseCase() } returns flowOf(testUser)
        every { getUserProfileUseCase("user_456") } returns flowOf(completedProfile)

        val viewModel = AuthGateViewModel(observeAuthStateUseCase, getUserProfileUseCase)

        viewModel.gateState.test {
            val item = awaitItem()
            val finalState = if (item is AuthGateState.Loading) awaitItem() else item
            assertTrue(finalState is AuthGateState.Authenticated)
            assertEquals(testUser, (finalState as AuthGateState.Authenticated).user)
            assertEquals(completedProfile, (finalState as AuthGateState.Authenticated).profile)
        }
    }
}
