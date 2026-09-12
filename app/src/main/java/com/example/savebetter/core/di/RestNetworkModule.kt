package com.example.savebetter.core.di

import com.example.savebetter.core.data.remote.rest.api.MockSaveBetterRestApi
import com.example.savebetter.core.data.remote.rest.api.SaveBetterRestApi
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module providing the REST API client.
 *
 * Currently binds [MockSaveBetterRestApi] as the reference in-memory implementation.
 * When integrating with a real REST backend (Django REST Framework, FastAPI, Spring Boot, etc.),
 * swap this binding with your Retrofit / Ktor [SaveBetterRestApi] implementation.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RestNetworkModule {

    @Binds
    @Singleton
    abstract fun bindSaveBetterRestApi(
        impl: MockSaveBetterRestApi
    ): SaveBetterRestApi
}
