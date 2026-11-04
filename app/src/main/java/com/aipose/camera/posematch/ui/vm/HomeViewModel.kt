package com.aipose.camera.posematch.ui.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aipose.camera.posematch.domain.models.AnalyticsEvent
import com.aipose.camera.posematch.domain.models.Capture
import com.aipose.camera.posematch.domain.models.Pose
import com.aipose.camera.posematch.domain.models.PoseLockAccess
import com.aipose.camera.posematch.domain.usecase.CaptureProgressUseCase
import com.aipose.camera.posematch.domain.usecase.CaptureUseCase
import com.aipose.camera.posematch.domain.usecase.FavoritePoseUseCase
import com.aipose.camera.posematch.domain.usecase.PoseLibraryUseCase
import com.aipose.camera.posematch.domain.usecase.PoseLockUseCase
import com.aipose.camera.posematch.domain.usecase.PremiumSubscriptionUseCase
import com.aipose.camera.posematch.domain.usecase.TrackEventUseCase
import com.aipose.camera.posematch.ui.models.PoseCategories
import com.aipose.camera.posematch.ui.screens.home.models.HomeHero
import com.aipose.camera.posematch.ui.screens.home.models.HomeUiState
import com.aipose.camera.posematch.ui.screens.home.models.HomeUnlockTarget
import com.aipose.camera.posematch.ui.screens.home.models.HomeUnlockedPose
import com.aipose.camera.posematch.ui.screens.poseAlbum.models.PoseCategoryCount
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.TimeMark
import kotlin.time.TimeSource
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.transformLatest
import kotlinx.coroutines.launch

private data class PosesWithHero(
    val poses: List<Pose>,
    val shuffledPoses: List<Pose>,
    val hero: Pose?
)

private data class HeroUnlock(
    val lockedPose: Pose? = null,
    val target: HomeUnlockTarget = HomeUnlockTarget.Camera
)

class HomeViewModel(
    private val poseLibraryUseCase: PoseLibraryUseCase,
    private val captureUseCase: CaptureUseCase,
    private val captureProgressUseCase: CaptureProgressUseCase,
    private val poseLockUseCase: PoseLockUseCase,
    private val favoritePoseUseCase: FavoritePoseUseCase,
    premiumSubscriptionUseCase: PremiumSubscriptionUseCase,
    private val trackEventUseCase: TrackEventUseCase
) : ViewModel() {

    val showProBadge: StateFlow<Boolean> = premiumSubscriptionUseCase.observeFreeUser()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(SUBSCRIPTION_TIMEOUT), false)

    private val _unlockedPoseToOpen = MutableStateFlow<HomeUnlockedPose?>(null)
    val unlockedPoseToOpen = _unlockedPoseToOpen.asStateFlow()

    private val _selectedCategory = MutableStateFlow<String?>(null)
    val selectedCategory = _selectedCategory.asStateFlow()

    private val heroUnlock = MutableStateFlow(HeroUnlock())

    private var heroPoseId: Int? = null
    private var heroShownAt: TimeMark? = null

    private val savedPoseIds: StateFlow<Set<Int>> = favoritePoseUseCase.observeFavorites()
        .map { favorites -> favorites.keys }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(SUBSCRIPTION_TIMEOUT), emptySet())

    val uiState: StateFlow<HomeUiState> = combine(
        posesWithRotatingHero(),
        captureUseCase.observeCaptures(),
        poseLockUseCase.observeAccess(),
        savedPoseIds,
        _selectedCategory,
        heroUnlock
    ) { flows ->
        @Suppress("UNCHECKED_CAST")
        val posesWithHero = flows[0] as PosesWithHero
        @Suppress("UNCHECKED_CAST")
        val captures = flows[1] as List<Capture>
        @Suppress("UNCHECKED_CAST")
        val access = flows[2] as PoseLockAccess
        @Suppress("UNCHECKED_CAST")
        val saved = flows[3] as Set<Int>
        @Suppress("UNCHECKED_CAST")
        val category = flows[4] as String?
        @Suppress("UNCHECKED_CAST")
        val unlock = flows[5] as HeroUnlock
        contentOf(posesWithHero, captures, access, saved, category, unlock)
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(SUBSCRIPTION_TIMEOUT),
        HomeUiState.Loading
    )

    init {
        track(AnalyticsEvent.Home)
    }

    fun onHeroTapped() {
        track(AnalyticsEvent.HomeHero)
    }

    fun onCategoryTapped() {
        track(AnalyticsEvent.HomeCategory)
    }

    fun selectCategory(category: String?) {
        _selectedCategory.value = category
    }

    fun toggleSaved(pose: Pose) {
        viewModelScope.launch {
            favoritePoseUseCase.setFavorite(pose.id, pose.id !in savedPoseIds.value)
        }
    }

    fun showLockedPose(pose: Pose, target: HomeUnlockTarget) {
        heroUnlock.value = HeroUnlock(lockedPose = pose, target = target)
    }

    fun dismissLockedPose() {
        heroUnlock.value = HeroUnlock()
    }

    fun unlockAfterRewardedAd(pose: Pose, target: HomeUnlockTarget) {
        heroUnlock.value = HeroUnlock()
        viewModelScope.launch {
            poseLockUseCase.unlockWithRewardedAd(pose.id)
            _unlockedPoseToOpen.value = HomeUnlockedPose(poseId = pose.id, target = target)
        }
    }

    fun consumeUnlockedPose() {
        _unlockedPoseToOpen.value = null
    }

    private fun track(event: AnalyticsEvent) {
        viewModelScope.launch { trackEventUseCase.track(event) }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun posesWithRotatingHero(): Flow<PosesWithHero> =
        poseLibraryUseCase.observePoses().transformLatest { poses ->
            val shuffledPoses = poses.shuffled()
            while (true) {
                val hero = currentOrNextHero(poses)
                emit(PosesWithHero(poses, shuffledPoses, hero))
                if (hero == null) return@transformLatest
                delay(HERO_ROTATION_INTERVAL - heroShownFor())
            }
        }

    private fun currentOrNextHero(poses: List<Pose>): Pose? {
        val current = poses.firstOrNull { it.id == heroPoseId }
        if (current != null && heroShownFor() < HERO_ROTATION_INTERVAL) return current
        val next = poseLibraryUseCase.nextHeroPose(poses, heroPoseId)
        heroPoseId = next?.id
        heroShownAt = TimeSource.Monotonic.markNow()
        return next
    }

    private fun heroShownFor(): Duration = heroShownAt?.elapsedNow() ?: HERO_ROTATION_INTERVAL

    private fun contentOf(
        posesWithHero: PosesWithHero,
        captures: List<Capture>,
        access: PoseLockAccess,
        saved: Set<Int>,
        selectedCategory: String?,
        unlock: HeroUnlock
    ): HomeUiState.Content {
        val poses = posesWithHero.poses
        val lockedIds = access.lockedIdsIn(poses)
        val filteredPoses = if (selectedCategory == null) {
            posesWithHero.shuffledPoses
        } else {
            poses.filter { it.category == selectedCategory }
        }
        return HomeUiState.Content(
            hero = posesWithHero.hero?.let { pose ->
                HomeHero(pose = pose, isLocked = pose.id in lockedIds)
            },
            categories = poses.categoryCounts(),
            selectedCategory = selectedCategory,
            poses = filteredPoses,
            savedPoseIds = saved,
            lockedPoseIds = lockedIds,
            progress = captureProgressUseCase.progressOf(captures),
            lockedPose = unlock.lockedPose,
            unlockTarget = unlock.target
        )
    }

    private fun List<Pose>.categoryCounts(): List<PoseCategoryCount> {
        val counts = groupingBy { it.category }.eachCount()
        val ordered = PoseCategories.displayOrder.filter { it in counts } +
            counts.keys.filter { it !in PoseCategories.displayOrder }
        return ordered.map { category -> PoseCategoryCount(category, counts.getValue(category)) }
    }

    private companion object {
        const val SUBSCRIPTION_TIMEOUT = 5_000L
        val HERO_ROTATION_INTERVAL = 1.minutes
    }
}
