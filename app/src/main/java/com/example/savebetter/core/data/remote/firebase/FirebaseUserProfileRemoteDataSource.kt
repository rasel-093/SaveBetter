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

    override suspend fun deleteUserData(userId: String): Result<Unit> = runCatching {
        val userDoc = firestore.collection("users").document(userId)
        val collections = listOf(
            "expenses",
            "categories",
            "weekly_targets",
            "monthly_targets",
            "salary_hand_records",
            "debts",
            "profile"
        )

        for (colName in collections) {
            val snapshot = userDoc.collection(colName).get().await()
            if (!snapshot.isEmpty) {
                snapshot.documents.chunked(450).forEach { chunk ->
                    val batch = firestore.batch()
                    chunk.forEach { doc -> batch.delete(doc.reference) }
                    batch.commit().await()
                }
            }
        }
        // Delete the root user document
        userDoc.delete().await()
    }
}

