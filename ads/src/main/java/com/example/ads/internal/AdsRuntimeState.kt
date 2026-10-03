package com.example.ads.internal

import android.os.SystemClock
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger

internal class AdsRuntimeState {

    private val _sdkInitialized = MutableStateFlow(false)
    val sdkInitialized: StateFlow<Boolean> = _sdkInitialized.asStateFlow()

    private val _canRequestAds = MutableStateFlow(false)
    val canRequestAds: StateFlow<Boolean> = _canRequestAds.asStateFlow()

    private val _foreground = MutableStateFlow(true)
    val foreground: StateFlow<Boolean> = _foreground.asStateFlow()

    private val _fullScreenAdVisible = MutableStateFlow(false)
    val fullScreenAdVisible: StateFlow<Boolean> = _fullScreenAdVisible.asStateFlow()

    private val fullScreenLock = AtomicBoolean(false)
    private val interstitialAttempts = AtomicInteger(0)

    @Volatile
    private var lastFullScreenShownAt = 0L

    fun setSdkInitialized(value: Boolean) {
        _sdkInitialized.value = value
    }

    fun setCanRequestAds(value: Boolean) {
        _canRequestAds.value = value
    }

    fun setForeground(value: Boolean) {
        _foreground.value = value
    }

    fun tryAcquireFullScreen(): Boolean = fullScreenLock.compareAndSet(false, true)

    fun markFullScreenVisible() {
        _fullScreenAdVisible.value = true
        lastFullScreenShownAt = SystemClock.elapsedRealtime()
    }

    fun releaseFullScreen() {
        _fullScreenAdVisible.value = false
        lastFullScreenShownAt = SystemClock.elapsedRealtime()
        fullScreenLock.set(false)
    }

    fun enoughTimeSinceLastFullScreenAd(minIntervalMs: Long): Boolean {
        if (minIntervalMs <= 0L) return true
        if (lastFullScreenShownAt == 0L) return true
        return SystemClock.elapsedRealtime() - lastFullScreenShownAt >= minIntervalMs
    }

    fun passesInterstitialFrequency(frequency: Int): Boolean {
        if (frequency <= 1) return true
        return (interstitialAttempts.getAndIncrement() % frequency) == 0
    }
}
