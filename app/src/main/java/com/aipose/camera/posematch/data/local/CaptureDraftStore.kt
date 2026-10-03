package com.aipose.camera.posematch.data.local

import com.aipose.camera.posematch.domain.models.CaptureDraft
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class CaptureDraftStore {

    private val _draft = MutableStateFlow<CaptureDraft?>(null)
    val draft: StateFlow<CaptureDraft?> = _draft.asStateFlow()

    fun put(draft: CaptureDraft) {
        _draft.value = draft
    }

    fun clear() {
        _draft.value = null
    }
}
