package com.aipose.camera.posematch.ui.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aipose.camera.posematch.domain.models.Pose
import com.aipose.camera.posematch.domain.usecase.FavoritePoseUseCase
import com.aipose.camera.posematch.domain.usecase.PoseLibraryUseCase
import com.aipose.camera.posematch.ui.graph.NavRoute
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class PoseAlbumViewModel(
    private val poseLibraryUseCase: PoseLibraryUseCase,
    private val favoritePoseUseCase: FavoritePoseUseCase
) : ViewModel() {

    val savedPoseIds: StateFlow<Set<Int>> = favoritePoseUseCase.observeFavorites()
        .map { favorites -> favorites.keys }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(SUBSCRIPTION_TIMEOUT), emptySet())

    fun toggleSaved(pose: Pose) {
        viewModelScope.launch {
            favoritePoseUseCase.setFavorite(pose.id, pose.id !in savedPoseIds.value)
        }
    }


    private val cachedCategories = mutableMapOf<String, StateFlow<List<Pose>>>()

    fun posesFor(category: String): StateFlow<List<Pose>> = cachedCategories.getOrPut(category) {
        poseLibraryUseCase.observePoses()
            .map { poses ->
                if (category == NavRoute.PoseAlbumScreenRoute.ALL_CATEGORIES) {
                    poses
                } else {
                    poses.filter { pose -> pose.category == category }
                }
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(SUBSCRIPTION_TIMEOUT), emptyList())
    }

    private companion object {
        const val SUBSCRIPTION_TIMEOUT = 5_000L
    }
}
