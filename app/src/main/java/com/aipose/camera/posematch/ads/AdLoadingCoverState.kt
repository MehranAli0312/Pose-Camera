package com.aipose.camera.posematch.ads

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class AdLoadingCoverState {

    private val _cover = MutableStateFlow<AdLoadingCover?>(null)

    val cover: StateFlow<AdLoadingCover?> = _cover.asStateFlow()

    fun show(cover: AdLoadingCover) {
        _cover.value = cover
    }

    fun hide(cover: AdLoadingCover) {
        _cover.update { current -> if (current == cover) null else current }
    }
}
