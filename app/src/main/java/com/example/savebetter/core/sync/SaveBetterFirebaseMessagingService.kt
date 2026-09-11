package com.example.savebetter.core.sync

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

/**
 * Placeholder Firebase Cloud Messaging service.
 *
 * FCM is registered in the manifest to allow future server-triggered
 * push notifications (Step 12). This class is intentionally minimal
 * at Step 0.
 *
 * FCM is only used for *server-originated* notifications.
 * Locally scheduled notifications (weekly/monthly warnings, reconciliation
 * reminders) are handled by WorkManager — they do NOT require FCM.
 *
 * The Firebase-specific implementation stays inside this class and is
 * never exposed to ViewModels, domain, or UI layers.
 */
class SaveBetterFirebaseMessagingService : FirebaseMessagingService() {

    /**
     * Called when the FCM token is refreshed.
     * The new token must be sent to the backend so it can target this device.
     * Implemented in Step 12.
     */
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        // TODO Step 12: upload token to Firestore user document
    }

    /**
     * Called when a FCM message is received while the app is in the foreground.
     * Implemented in Step 12.
     */
    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        // TODO Step 12: handle server-triggered notifications
    }
}
