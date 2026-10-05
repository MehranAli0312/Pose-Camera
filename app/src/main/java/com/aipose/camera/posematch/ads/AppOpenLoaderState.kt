package com.aipose.camera.posematch.ads

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AppOpenLoaderState {

    private val _isVisible = MutableStateFlow(false)

    val isVisible: StateFlow<Boolean> = _isVisible.asStateFlow()

    fun setVisible(visible: Boolean) {
        _isVisible.value = visible
    }
}
