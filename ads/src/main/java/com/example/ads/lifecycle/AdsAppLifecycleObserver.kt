package com.example.ads.lifecycle

import android.app.Activity
import android.app.Application
import android.os.Bundle
import android.os.SystemClock
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.example.ads.AdPlacement
import com.example.ads.AdResult
import com.example.ads.AdsManager
import com.example.ads.internal.AdsRuntimeState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class AdsAppLifecycleObserver internal constructor(
    private val adsManager: AdsManager,
    private val state: AdsRuntimeState,
    private val scope: CoroutineScope,
) : DefaultLifecycleObserver, Application.ActivityLifecycleCallbacks {

    private var currentActivity: Activity? = null

    private var wentToBackground = false

    private var appOpenJob: Job? = null

    private var externalScreenLaunchedAt = NOT_LAUNCHED

    private var skipNextAppOpen = false

    private var startedActivities = 0

    var appOpenEnabled: () -> Boolean = { false }

    var appOpenPlacement: AdPlacement = AdPlacement.Default

    var onAppOpenFinished: (AdResult) -> Unit = {}

    var appOpenLoadTimeoutMs: () -> Long? = { null }

    var onAppOpenCoverChanged: (Boolean) -> Unit = {}

    @Volatile
    private var appOpenLoading = false

    private var appOpenCovered = false

    fun register(application: Application) {
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
        application.registerActivityLifecycleCallbacks(this)
    }

    fun unregister(application: Application) {
        ProcessLifecycleOwner.get().lifecycle.removeObserver(this)
        application.unregisterActivityLifecycleCallbacks(this)
        currentActivity = null
    }

    /**
     * Call right before the app itself opens another app's screen (system settings, share sheet,
     * store page, ...). Returning from that screen is part of the user's task, so the next
     * app-open is skipped. The mark only counts if the app goes to background shortly after,
     * so a launch that never leaves the app cannot swallow a later genuine return.
     */
    fun onExternalScreenLaunched() {
        externalScreenLaunchedAt = SystemClock.elapsedRealtime()
        if (wentToBackground) skipNextAppOpen = true
    }

    override fun onStart(owner: LifecycleOwner) {
        state.setForeground(true)
        val returningFromBackground = wentToBackground
        val skip = skipNextAppOpen
        wentToBackground = false
        skipNextAppOpen = false
        externalScreenLaunchedAt = NOT_LAUNCHED
        if (returningFromBackground && !skip) showAppOpenOnResume()
    }

    override fun onStop(owner: LifecycleOwner) {
        state.setForeground(false)
        wentToBackground = true
        if (appOpenLoading) appOpenJob?.cancel()
        if (externalScreenLaunchedAt != NOT_LAUNCHED &&
            SystemClock.elapsedRealtime() - externalScreenLaunchedAt <= EXTERNAL_LAUNCH_WINDOW_MS
        ) {
            skipNextAppOpen = true
        }
    }

    private fun showAppOpenOnResume() {
        if (appOpenJob?.isActive == true) return
        if (!appOpenEnabled() || !adsManager.isReady.value || adsManager.isPro.value) return
        val activity = currentActivity ?: return

        appOpenJob = scope.launch(Dispatchers.Main.immediate) {
            val result = try {
                adsManager.loadAndShow(
                    activity = currentActivity ?: activity,
                    placement = appOpenPlacement,
                    onShown = { appOpenLoading = false },
                    loadTimeoutMs = appOpenLoadTimeoutMs(),
                    onLoadStarted = {
                        appOpenLoading = true
                        setAppOpenCovered(true)
                    },
                )
            } finally {
                appOpenLoading = false
                setAppOpenCovered(false)
            }
            onAppOpenFinished(result)
        }
    }

    private fun setAppOpenCovered(covered: Boolean) {
        if (appOpenCovered == covered) return
        appOpenCovered = covered
        onAppOpenCoverChanged(covered)
    }

    override fun onActivityStarted(activity: Activity) {
        startedActivities++
        state.setForeground(true)
        if (!state.fullScreenAdVisible.value) currentActivity = activity
    }

    override fun onActivityStopped(activity: Activity) {
        startedActivities = (startedActivities - 1).coerceAtLeast(0)
        if (startedActivities == 0) state.setForeground(false)
    }

    override fun onActivityDestroyed(activity: Activity) {
        if (currentActivity === activity) currentActivity = null
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) = Unit
    override fun onActivityResumed(activity: Activity) = Unit
    override fun onActivityPaused(activity: Activity) = Unit
    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) = Unit

    private companion object {
        const val NOT_LAUNCHED = -1L
        const val EXTERNAL_LAUNCH_WINDOW_MS = 10_000L
    }
}
