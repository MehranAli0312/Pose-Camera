package com.aipose.camera.posematch.data.analytics

import com.google.firebase.Firebase
import com.google.firebase.analytics.analytics
import com.google.firebase.analytics.logEvent

class AnalyticsDataSource {

    private val analytics by lazy { Firebase.analytics }

    fun logEvent(eventName: String, params: Map<String, String>) {
        analytics.logEvent(eventName) {
            params.forEach { (key, value) -> param(key, value) }
        }
    }

    fun setUserProperty(name: String, value: String) {
        analytics.setUserProperty(name, value)
    }
}
