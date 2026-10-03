package com.aipose.camera.posematch.ads

import com.example.ads.ProStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ProStatusStore {

    private val _status = MutableStateFlow(ProStatus.UNKNOWN)

    val status: StateFlow<ProStatus> = _status.asStateFlow()

    fun resolve(isPro: Boolean) {
        _status.value = if (isPro) ProStatus.PRO else ProStatus.FREE
    }

    fun resolveUnknownAsFree() {
        if (_status.value == ProStatus.UNKNOWN) _status.value = ProStatus.FREE
    }
}
