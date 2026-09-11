package com.example.savebetter

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

/**
 * SaveBetter Application entry point.
 *
 * Annotated with @HiltAndroidApp to trigger Hilt's code generation and
 * set up the application-level dependency injection container.
 *
 * Firebase is initialised automatically by the google-services Gradle plugin;
 * no explicit FirebaseApp.initializeApp() call is required.
 *
 * WorkManager uses HiltWorkerFactory so Hilt can inject dependencies into
 * CoroutineWorker subclasses (SyncWorker etc., added in Step 14).
 */
@HiltAndroidApp
class SaveBetterApplication : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
}
