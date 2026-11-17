package com.aipose.camera.posematch.ui.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aipose.camera.posematch.domain.models.AnalyticsEvent
import com.aipose.camera.posematch.domain.models.Pose
import com.aipose.camera.posematch.domain.usecase.FavoritePoseUseCase
import com.aipose.camera.posematch.domain.usecase.PoseLibraryUseCase
import com.aipose.camera.posematch.domain.usecase.PoseLockUseCase
import com.aipose.camera.posematch.domain.usecase.PremiumSubscriptionUseCase
import com.aipose.camera.posematch.domain.usecase.TrackEventUseCase
import com.aipose.camera.posematch.ui.models.PoseCategories
import com.aipose.camera.posematch.ui.screens.home.models.HomeUiState
import com.aipose.camera.posematch.ui.screens.poseAlbum.models.PoseCategoryCount
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HomeViewModel(
    private val poseLibraryUseCase: PoseLibraryUseCase,
    private val poseLockUseCase: PoseLockUseCase,
    private val favoritePoseUseCase: FavoritePoseUseCase,
    premiumSubscriptionUseCase: PremiumSubscriptionUseCase,
    private val trackEventUseCase: TrackEventUseCase,
) : ViewModel() {

    val showProBadge: StateFlow<Boolean> = premiumSubscriptionUseCase.observeFreeUser()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(SUBSCRIPTION_TIMEOUT), false)

    private val _unlockedPoseToOpen = MutableStateFlow<Int?>(null)
    val unlockedPoseToOpen = _unlockedPoseToOpen.asStateFlow()

    private val selectedCategory = MutableStateFlow(PoseCategories.COUPLE)
    private val lockedPose = MutableStateFlow<Pose?>(null)

    private val savedPoseIds: StateFlow<Set<Int>> = favoritePoseUseCase.observeFavorites()
        .map { favorites -> favorites.keys }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(SUBSCRIPTION_TIMEOUT), emptySet())

    val uiState: StateFlow<HomeUiState> = combine(
        poseLibraryUseCase.observePoses(),
        poseLockUseCase.observeAccess(),
        savedPoseIds,
        selectedCategory,
        lockedPose,
    ) { poses, access, saved, category, poseToUnlock ->
        val categories = poses.categoryCounts()
        val resolvedCategory = category.takeIf { selected ->
            categories.any { item -> item.category == selected }
        } ?: categories.firstOrNull()?.category
        val visiblePoses = poses.filter { pose -> pose.category == resolvedCategory }
        HomeUiState.Content(
            categories = categories,
            selectedCategory = resolvedCategory,
            poses = visiblePoses,
            savedPoseIds = saved,
            lockedPoseIds = access.lockedIdsIn(poses),
            lockedPose = poseToUnlock,
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(SUBSCRIPTION_TIMEOUT),
        HomeUiState.Loading,
    )

    init {
        viewModelScope.launch { trackEventUseCase.track(AnalyticsEvent.Home) }
    }

    fun selectCategory(category: String) {
        selectedCategory.value = category
    }

    fun toggleSaved(pose: Pose) {
        viewModelScope.launch {
            favoritePoseUseCase.setFavorite(pose.id, pose.id !in savedPoseIds.value)
        }
    }

    fun showLockedPose(pose: Pose) {
        lockedPose.value = pose
    }

    fun dismissLockedPose() {
        lockedPose.value = null
    }

    fun unlockAfterRewardedAd(pose: Pose) {
        lockedPose.value = null
        viewModelScope.launch {
            poseLockUseCase.unlockWithRewardedAd(pose.id)
            _unlockedPoseToOpen.value = pose.id
        }
    }

    fun consumeUnlockedPose() {
        _unlockedPoseToOpen.value = null
    }

    private fun List<Pose>.categoryCounts(): List<PoseCategoryCount> {
        val counts = groupingBy { it.category }.eachCount()
        val ordered = listOf(PoseCategories.COUPLE).filter { it in counts } +
            PoseCategories.displayOrder.filter { it in counts && it != PoseCategories.COUPLE } +
            counts.keys.filter { it !in PoseCategories.displayOrder }
        return ordered.map { category -> PoseCategoryCount(category, counts.getValue(category)) }
    }

    private companion object {
        const val SUBSCRIPTION_TIMEOUT = 5_000L
    }
}
