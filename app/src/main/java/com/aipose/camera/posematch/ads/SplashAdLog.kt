package com.aipose.camera.posematch.ads

import android.os.Process
import android.os.SystemClock
import android.util.Log
import com.aipose.camera.posematch.BuildConfig

object SplashAdLog {

    private const val TAG = "SplashAd"

    fun d(message: String) {
        if (!BuildConfig.DEBUG) return
        val sinceProcessStart = SystemClock.elapsedRealtime() - Process.getStartElapsedRealtime()
        Log.d(TAG, "+${sinceProcessStart}ms $message")
    }
}
