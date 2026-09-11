package com.example.savebetter.core.data.repository

import app.cash.turbine.test
import com.example.savebetter.core.data.local.dao.UserDao
import com.example.savebetter.core.data.mapper.toEntity
import com.example.savebetter.core.data.remote.UserProfileRemoteDataSource
import com.example.savebetter.core.domain.model.SyncState
import com.example.savebetter.core.domain.model.UserProfile
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Unit tests for [UserProfileRepositoryImpl].
 */
class UserProfileRepositoryImplTest {

    private val userDao: UserDao = mockk(relaxed = true)
    private val remoteDataSource: UserProfileRemoteDataSource = mockk(relaxed = true)
    private val repository = UserProfileRepositoryImpl(userDao, remoteDataSource)

    private val testUser = UserProfile(
        id = "user_999",
        name = "Kazi Nazrul",
        email = "kazi@example.com",
        monthlySalaryMinor = 8000000L,
        preferredLanguage = "bn",
        onboardingCompleted = false
    )

    @Test
    fun `saveUserProfile writes to Room first with PENDING then updates to SYNCED on success`() = runTest {
        coEvery { remoteDataSource.saveUserProfile(any()) } returns Result.success(Unit)

        repository.saveUserProfile(testUser)

        coVerify {
            userDao.upsertUser(match { it.id == "user_999" && it.syncStatus == SyncState.PENDING })
        }
        coVerify {
            userDao.updateSyncStatus("user_999", SyncState.SYNCED, any())
        }
    }

    @Test
    fun `observeUserProfile emits mapped domain user`() = runTest {
        every { userDao.observeUser("user_999") } returns flowOf(testUser.toEntity())

        repository.observeUserProfile("user_999").test {
            val user = awaitItem()
            assertEquals("user_999", user?.id)
            assertEquals("Kazi Nazrul", user?.name)
            awaitComplete()
        }
    }
}
