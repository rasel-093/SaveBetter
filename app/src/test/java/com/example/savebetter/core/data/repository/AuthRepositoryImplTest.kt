package com.example.savebetter.core.data.repository

import app.cash.turbine.test
import com.example.savebetter.core.auth.model.AuthException
import com.example.savebetter.core.auth.model.AuthUser
import com.example.savebetter.core.data.remote.auth.AuthRemoteDataSource
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class AuthRepositoryImplTest {

    private val remoteDataSource: AuthRemoteDataSource = mockk(relaxed = true)
    private lateinit var repository: AuthRepositoryImpl

    private val testUser = AuthUser(
        id = "user_123",
        email = "user@example.com",
        displayName = "Test User"
    )

    @Before
    fun setUp() {
        repository = AuthRepositoryImpl(remoteDataSource)
    }

    @Test
    fun `observeAuthState emits user from remote data source`() = runTest {
        every { remoteDataSource.observeAuthState() } returns flowOf(testUser)

        repository.observeAuthState().test {
            val item = awaitItem()
            assertEquals(testUser, item)
            awaitComplete()
        }
    }

    @Test
    fun `signIn delegates to remote data source successfully`() = runTest {
        coEvery { remoteDataSource.signIn("user@example.com", "password123") } returns Result.success(testUser)

        val result = repository.signIn("user@example.com", "password123")

        assertTrue(result.isSuccess)
        assertEquals(testUser, result.getOrNull())
        coVerify(exactly = 1) { remoteDataSource.signIn("user@example.com", "password123") }
    }

    @Test
    fun `signIn returns failure when remote fails`() = runTest {
        val error = AuthException("Invalid credentials")
        coEvery { remoteDataSource.signIn("user@example.com", "wrong") } returns Result.failure(error)

        val result = repository.signIn("user@example.com", "wrong")

        assertTrue(result.isFailure)
        assertEquals("Invalid credentials", result.exceptionOrNull()?.message)
    }

    @Test
    fun `signUp delegates to remote data source`() = runTest {
        coEvery { remoteDataSource.signUp("new@example.com", "password123") } returns Result.success(testUser)

        val result = repository.signUp("new@example.com", "password123")

        assertTrue(result.isSuccess)
        assertEquals(testUser, result.getOrNull())
    }

    @Test
    fun `signInWithGoogle delegates to remote data source`() = runTest {
        coEvery { remoteDataSource.signInWithGoogle("token_abc") } returns Result.success(testUser)

        val result = repository.signInWithGoogle("token_abc")

        assertTrue(result.isSuccess)
        assertEquals(testUser, result.getOrNull())
    }

    @Test
    fun `sendPasswordResetEmail delegates to remote data source`() = runTest {
        coEvery { remoteDataSource.sendPasswordResetEmail("user@example.com") } returns Result.success(Unit)

        val result = repository.sendPasswordResetEmail("user@example.com")

        assertTrue(result.isSuccess)
    }

    @Test
    fun `signOut delegates to remote data source`() = runTest {
        repository.signOut()

        coVerify(exactly = 1) { remoteDataSource.signOut() }
    }
}
