package com.aipose.camera.posematch.ui.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aipose.camera.posematch.domain.models.Pose
import com.aipose.camera.posematch.domain.usecase.CaptureProgressUseCase
import com.aipose.camera.posematch.domain.usecase.CaptureUseCase
import com.aipose.camera.posematch.domain.usecase.FavoritePoseUseCase
import com.aipose.camera.posematch.domain.usecase.PoseLibraryUseCase
import com.aipose.camera.posematch.ui.screens.poseDetail.models.PoseDetailUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class PoseDetailViewModel(
    private val poseLibraryUseCase: PoseLibraryUseCase,
    private val favoritePoseUseCase: FavoritePoseUseCase,
    private val captureProgressUseCase: CaptureProgressUseCase,
    captureUseCase: CaptureUseCase
) : ViewModel() {

    private val poseId = MutableStateFlow<Int?>(null)

    private val _shareImagePath = MutableStateFlow<String?>(null)
    val shareImagePath = _shareImagePath.asStateFlow()

    val uiState: StateFlow<PoseDetailUiState> = combine(
        poseLibraryUseCase.observePoses(),
        captureUseCase.observeCaptures(),
        favoritePoseUseCase.observeFavorites(),
        poseId
    ) { poses, captures, favorites, id ->
        val pose = poses.firstOrNull { it.id == id }
            ?: return@combine PoseDetailUiState.Loading
        PoseDetailUiState.Content(
            pose = pose,
            categoryCount = poses.count { it.category == pose.category },
            tryCount = captures.count { it.poseId == pose.id },
            bestMatch = captureProgressUseCase.bestMatchFor(captures, pose.id),
            isSaved = pose.id in favorites
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(SUBSCRIPTION_TIMEOUT),
        PoseDetailUiState.Loading
    )

    fun onPoseRequested(id: Int) {
        if (poseId.value == id) return
        poseId.value = id
    }

    fun toggleSaved() {
        val content = uiState.value as? PoseDetailUiState.Content ?: return
        viewModelScope.launch {
            favoritePoseUseCase.setFavorite(content.pose.id, !content.isSaved)
        }
    }

    fun share(pose: Pose) {
        viewModelScope.launch {
            _shareImagePath.value = poseLibraryUseCase.shareableImagePath(pose)
        }
    }

    fun consumeShareRequest() {
        _shareImagePath.value = null
    }

    private companion object {
        const val SUBSCRIPTION_TIMEOUT = 5_000L
    }
}
