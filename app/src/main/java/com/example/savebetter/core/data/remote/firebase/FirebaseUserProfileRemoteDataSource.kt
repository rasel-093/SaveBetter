package com.example.savebetter.core.data.remote.firebase

import com.example.savebetter.core.data.mapper.toDomain
import com.example.savebetter.core.data.mapper.toFirestoreDto
import com.example.savebetter.core.data.remote.UserProfileRemoteDataSource
import com.example.savebetter.core.data.remote.firebase.dto.UserFirestoreDto
import com.example.savebetter.core.domain.model.UserProfile
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Firebase Firestore implementation for user profile sync.
 *
 * User profile is strictly isolated to `/users/{userId}/profile/main`.
 */
@Singleton
class FirebaseUserProfileRemoteDataSource @Inject constructor(
    private val firestore: FirebaseFirestore
) : UserProfileRemoteDataSource {

    override suspend fun fetchUserProfile(userId: String): Result<UserProfile?> = runCatching {
        val snapshot = firestore.collection("users")
            .document(userId)
            .collection("profile")
            .document("main")
            .get()
            .await()

        if (snapshot.exists()) {
            snapshot.toObject(UserFirestoreDto::class.java)?.toDomain()
        } else {
            null
        }
    }

    override suspend fun saveUserProfile(profile: UserProfile): Result<Unit> = runCatching {
        val dto = profile.toFirestoreDto()
        firestore.collection("users")
            .document(profile.id)
            .collection("profile")
            .document("main")
            .set(dto, SetOptions.merge())
            .await()
    }
}
