package com.aipose.camera.posematch.ads

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class ProStatusForegroundObserver(
    private val proStatusRefresher: ProStatusRefresher,
    private val scope: CoroutineScope,
) : DefaultLifecycleObserver {

    private var wentToBackground = false

    private var refreshJob: Job? = null

    fun register() {
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
    }

    override fun onStop(owner: LifecycleOwner) {
        wentToBackground = true
    }

    override fun onStart(owner: LifecycleOwner) {
        if (!wentToBackground) return
        wentToBackground = false
        if (refreshJob?.isActive == true) return
        refreshJob = scope.launch {
            runCatching { proStatusRefresher.connectAndRefresh() }
        }
    }
}
