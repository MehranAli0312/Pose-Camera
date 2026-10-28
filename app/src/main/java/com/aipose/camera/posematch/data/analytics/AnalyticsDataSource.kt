package com.aipose.camera.posematch.data.analytics

import com.google.firebase.Firebase
import com.google.firebase.analytics.analytics

class AnalyticsDataSource {

    private val analytics by lazy { Firebase.analytics }

    fun logEvent(eventName: String) {
        analytics.logEvent(eventName, null)
    }
}
