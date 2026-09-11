package com.example.savebetter.core.di

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.PersistentCacheSettings
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module that provides Firebase service singletons.
 *
 * ┌──────────────────────────────────────────────────────────────────────┐
 * │  ARCHITECTURE BOUNDARY — CRITICAL                                    │
 * │                                                                      │
 * │  Firebase classes (FirebaseAuth, FirebaseFirestore, etc.) must ONLY  │
 * │  be injected into classes inside:                                    │
 * │    core/data/remote/firebase/                                        │
 * │                                                                      │
 * │  They must NEVER be injected into:                                   │
 * │    • Composables                                                     │
 * │    • ViewModels                                                      │
 * │    • Use cases                                                       │
 * │    • Repository interfaces                                           │
 * │    • Domain models                                                   │
 * │                                                                      │
 * │  This boundary ensures Firebase can later be replaced by a           │
 * │  Django REST API without modifying the UI or business logic.         │
 * └──────────────────────────────────────────────────────────────────────┘
 */
@Module
@InstallIn(SingletonComponent::class)
object FirebaseModule {

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()

    @Provides
    @Singleton
    fun provideFirebaseFirestore(): FirebaseFirestore {
        val firestore = FirebaseFirestore.getInstance()

        // Enable Firestore offline persistence with a 50 MB cache.
        // This ensures the app is usable without network connectivity.
        // Room remains the primary local source of truth for the UI;
        // Firestore's local cache is an additional offline layer for
        // sync operations.
        val cacheSettings = PersistentCacheSettings.newBuilder()
            .setSizeBytes(50L * 1024 * 1024) // 50 MB
            .build()

        firestore.firestoreSettings = FirebaseFirestoreSettings.Builder()
            .setLocalCacheSettings(cacheSettings)
            .build()

        return firestore
    }
}
