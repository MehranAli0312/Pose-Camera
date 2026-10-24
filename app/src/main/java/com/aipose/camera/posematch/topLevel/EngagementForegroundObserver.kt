package com.aipose.camera.posematch.topLevel

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.aipose.camera.posematch.domain.usecase.EngagementNotificationUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class EngagementForegroundObserver(
    private val engagementNotificationUseCase: EngagementNotificationUseCase,
    private val scope: CoroutineScope,
) : DefaultLifecycleObserver {

    fun register() {
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
    }

    override fun onStart(owner: LifecycleOwner) {
        scope.launch {
            runCatching { engagementNotificationUseCase.onAppForeground() }
        }
    }
}
