package com.aipose.camera.posematch.data.notification

import com.aipose.camera.posematch.domain.models.NotificationTarget
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.getAndUpdate

class NotificationLaunchStore {

    private val _pending = MutableStateFlow<NotificationTarget?>(null)
    val pending: StateFlow<NotificationTarget?> = _pending.asStateFlow()

    fun put(target: NotificationTarget) {
        _pending.value = target
    }

    fun consume(): NotificationTarget? = _pending.getAndUpdate { null }
}
