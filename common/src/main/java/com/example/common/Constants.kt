package com.example.common

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object Constants {
    private val _splashEnded = MutableStateFlow(false)
    val splashEnded: StateFlow<Boolean> = _splashEnded.asStateFlow()

    var splashEnd: Boolean
        get() = _splashEnded.value
        set(value) {
            _splashEnded.value = value
        }

    var SHOW_ONBOARD_AD_PAGE: Boolean = false
}
