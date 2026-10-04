package com.aipose.camera.posematch.ui.vm

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aipose.camera.posematch.R
import com.aipose.camera.posematch.domain.models.Capture
import com.aipose.camera.posematch.domain.models.Pose
import com.aipose.camera.posematch.domain.usecase.CaptureUseCase
import com.aipose.camera.posematch.domain.usecase.FavoritePoseUseCase
import com.aipose.camera.posematch.domain.usecase.PoseLibraryUseCase
import com.aipose.camera.posematch.ui.screens.saved.models.SavedPose
import com.aipose.camera.posematch.ui.screens.saved.models.SavedShot
import com.aipose.camera.posematch.ui.screens.saved.models.SavedSort
import com.aipose.camera.posematch.ui.screens.saved.models.SavedTab
import com.aipose.camera.posematch.ui.screens.saved.models.SavedUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SavedViewModel(
    private val context: Context,
    private val captureUseCase: CaptureUseCase,
    private val favoritePoseUseCase: FavoritePoseUseCase,
    poseLibraryUseCase: PoseLibraryUseCase
) : ViewModel() {

    private val selectedTab = MutableStateFlow(SavedTab.Shots)

    private val _sort = MutableStateFlow(SavedSort.Newest)
    val sort = _sort.asStateFlow()

    private val _isSortSheetVisible = MutableStateFlow(false)
    val isSortSheetVisible = _isSortSheetVisible.asStateFlow()

    val uiState: StateFlow<SavedUiState> = combine(
        captureUseCase.observeCaptures(),
        poseLibraryUseCase.observePoses(),
        favoritePoseUseCase.observeFavorites(),
        selectedTab,
        _sort
    ) { captures, poses, favorites, tab, sort ->
        SavedUiState.Content(
            selectedTab = tab,
            sort = sort,
            shots = captures.filter { it.isFavorite }.map { it.toSavedShot() }.sortedBy(sort),
            poses = poses.toSavedPoses(favorites).sortedBy(sort)
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(SUBSCRIPTION_TIMEOUT),
        SavedUiState.Loading
    )

    fun selectTab(tab: SavedTab) {
        selectedTab.value = tab
        if (tab == SavedTab.Poses && _sort.value.shotsOnly) _sort.value = SavedSort.Newest
    }

    fun selectSort(sort: SavedSort) {
        _sort.value = sort
        _isSortSheetVisible.value = false
    }

    fun showSortSheet() {
        _isSortSheetVisible.value = true
    }

    fun dismissSortSheet() {
        _isSortSheetVisible.value = false
    }

    fun unsaveShot(shot: SavedShot) {
        viewModelScope.launch { captureUseCase.toggleFavorite(shot.capture) }
    }

    fun unsavePose(saved: SavedPose) {
        viewModelScope.launch { favoritePoseUseCase.setFavorite(saved.pose.id, false) }
    }

    private fun Capture.toSavedShot(): SavedShot = SavedShot(
        capture = this,
        locationLabel = location.name ?: context.getString(R.string.unknown_location)
    )

    private fun List<Pose>.toSavedPoses(favorites: Map<Int, Long>): List<SavedPose> =
        mapNotNull { pose ->
            favorites[pose.id]?.let { savedAt -> SavedPose(pose = pose, savedAtMillis = savedAt) }
        }

    @JvmName("sortShots")
    private fun List<SavedShot>.sortedBy(sort: SavedSort): List<SavedShot> = when (sort) {
        SavedSort.Newest -> sortedByDescending { it.capture.capturedAtMillis }
        SavedSort.HighestMatch -> sortedByDescending { it.capture.matchScore }
        SavedSort.NameAsc -> sortedBy { it.capture.title.lowercase() }
    }

    @JvmName("sortPoses")
    private fun List<SavedPose>.sortedBy(sort: SavedSort): List<SavedPose> = when (sort) {
        SavedSort.Newest, SavedSort.HighestMatch -> sortedByDescending { it.savedAtMillis }
        SavedSort.NameAsc -> sortedBy { it.pose.title.lowercase() }
    }

    private companion object {
        const val SUBSCRIPTION_TIMEOUT = 5_000L
    }
}
