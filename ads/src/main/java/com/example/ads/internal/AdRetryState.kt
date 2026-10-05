package com.example.ads.internal

import android.os.SystemClock
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue

internal class AdRetryState {

    var attempt by mutableIntStateOf(0)
        private set

    @Volatile
    private var lastFailureAt = 0L

    fun markFailed() {
        lastFailureAt = SystemClock.elapsedRealtime()
    }

    fun tryRetry(requireGap: Boolean): Boolean {
        if (attempt >= MAX_RETRIES) return false
        if (requireGap && SystemClock.elapsedRealtime() - lastFailureAt < MIN_RETRY_GAP_MS) {
            return false
        }
        attempt++
        return true
    }

    private companion object {
        const val MAX_RETRIES = 3
        const val MIN_RETRY_GAP_MS = 30_000L
    }
}
