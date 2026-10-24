package com.aipose.camera.posematch.data.notification

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import org.koin.android.ext.android.inject

class PushMessagingService : FirebaseMessagingService() {

    private val pushNotificationDataSource: PushNotificationDataSource by inject()

    override fun onMessageReceived(message: RemoteMessage) {
        val notification = message.notification ?: return
        pushNotificationDataSource.post(notification.title, notification.body)
    }
}
