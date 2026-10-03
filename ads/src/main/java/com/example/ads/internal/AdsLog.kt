package com.example.ads.internal

import android.util.Log
import com.example.ads.AdsConfig

internal class AdsLog(private val config: AdsConfig) {

    fun d(message: String) {
        if (config.enableLogging) Log.d(TAG, message)
    }

    fun w(message: String, error: Throwable? = null) {
        if (config.enableLogging) Log.w(TAG, message, error)
    }

    private companion object {
        const val TAG = "Ads"
    }
}
