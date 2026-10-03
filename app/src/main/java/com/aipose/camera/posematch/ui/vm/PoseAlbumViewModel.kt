package com.aipose.camera.posematch.ui.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aipose.camera.posematch.domain.models.Pose
import com.aipose.camera.posematch.domain.usecase.PoseLibraryUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class PoseAlbumViewModel(private val poseLibraryUseCase: PoseLibraryUseCase) : ViewModel() {

    private val cachedCategories = mutableMapOf<String, StateFlow<List<Pose>>>()

    fun posesFor(category: String): StateFlow<List<Pose>> = cachedCategories.getOrPut(category) {
        poseLibraryUseCase.observePoses()
            .map { poses -> poses.filter { pose -> pose.category == category } }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(SUBSCRIPTION_TIMEOUT), emptyList())
    }

    private companion object {
        const val SUBSCRIPTION_TIMEOUT = 5_000L
    }
}
