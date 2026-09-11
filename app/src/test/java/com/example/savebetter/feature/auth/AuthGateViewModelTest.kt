package com.example.savebetter.feature.auth

import app.cash.turbine.test
import com.example.savebetter.core.auth.model.AuthUser
import com.example.savebetter.core.domain.usecase.auth.ObserveAuthStateUseCase
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

    private val testUser = AuthUser(
        id = "user_456",
        email = "persisted@example.com",
        displayName = "Persisted User"
    )

    @Test
    fun `gateState emits Authenticated when user is logged in`() = runTest {
        every { observeAuthStateUseCase() } returns flowOf(testUser)

        val viewModel = AuthGateViewModel(observeAuthStateUseCase)

        viewModel.gateState.test {
            // First item may be initial Loading or immediately Authenticated with UnconfinedTestDispatcher
            val item = awaitItem()
            if (item is AuthGateState.Loading) {
                val next = awaitItem()
                assertTrue(next is AuthGateState.Authenticated)
                assertEquals(testUser, (next as AuthGateState.Authenticated).user)
            } else {
                assertTrue(item is AuthGateState.Authenticated)
                assertEquals(testUser, (item as AuthGateState.Authenticated).user)
            }
        }
    }

    @Test
    fun `gateState emits Unauthenticated when user is null`() = runTest {
        every { observeAuthStateUseCase() } returns flowOf(null)

        val viewModel = AuthGateViewModel(observeAuthStateUseCase)

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
}
