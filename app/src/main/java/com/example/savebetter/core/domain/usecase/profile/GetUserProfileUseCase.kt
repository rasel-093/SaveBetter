package com.example.savebetter.core.domain.usecase.profile

import com.example.savebetter.core.domain.model.UserProfile
import com.example.savebetter.core.domain.repository.UserProfileRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case to observe or query the user's financial profile.
 */
class GetUserProfileUseCase @Inject constructor(
    private val userProfileRepository: UserProfileRepository
) {
    operator fun invoke(userId: String): Flow<UserProfile?> {
        return userProfileRepository.observeUserProfile(userId)
    }

    suspend fun getOnce(userId: String): UserProfile? {
        return userProfileRepository.getUserProfile(userId)
    }
}
