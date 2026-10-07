package com.aipose.camera.posematch.ui.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aipose.camera.posematch.domain.models.Capture
import com.aipose.camera.posematch.domain.models.Pose
import com.aipose.camera.posematch.domain.models.PoseLockAccess
import com.aipose.camera.posematch.domain.usecase.CaptureProgressUseCase
import com.aipose.camera.posematch.domain.usecase.CaptureUseCase
import com.aipose.camera.posematch.domain.usecase.PoseLibraryUseCase
import com.aipose.camera.posematch.domain.usecase.PoseLockUseCase
import com.aipose.camera.posematch.domain.usecase.PremiumSubscriptionUseCase
import com.aipose.camera.posematch.ui.screens.home.models.HomeDailyPose
import com.aipose.camera.posematch.ui.screens.home.models.HomeHero
import com.aipose.camera.posematch.ui.screens.home.models.HomeQuickAction
import com.aipose.camera.posematch.ui.screens.home.models.HomeQuickActionId
import com.aipose.camera.posematch.ui.screens.home.models.HomeUiState
import com.aipose.camera.posematch.ui.screens.home.models.HomeUnlockTarget
import com.aipose.camera.posematch.ui.screens.home.models.HomeUnlockedPose
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
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.transformLatest
import kotlinx.coroutines.launch

private data class PosesWithHero(
    val poses: List<Pose>,
    val hero: Pose?
)

private data class HeroUnlock(
    val lockedPose: Pose? = null,
    val target: HomeUnlockTarget = HomeUnlockTarget.Camera,
    val isAdLoading: Boolean = false
)

class HomeViewModel(
    private val poseLibraryUseCase: PoseLibraryUseCase,
    private val captureUseCase: CaptureUseCase,
    private val captureProgressUseCase: CaptureProgressUseCase,
    private val poseLockUseCase: PoseLockUseCase,
    premiumSubscriptionUseCase: PremiumSubscriptionUseCase
) : ViewModel() {

    val showProBadge: StateFlow<Boolean> = premiumSubscriptionUseCase.observeFreeUser()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(SUBSCRIPTION_TIMEOUT), false)

    private val _importedPose = MutableStateFlow<Pose?>(null)
    val importedPose = _importedPose.asStateFlow()

    private val _importFailed = MutableStateFlow(false)
    val importFailed = _importFailed.asStateFlow()

    private val _unlockedPoseToOpen = MutableStateFlow<HomeUnlockedPose?>(null)
    val unlockedPoseToOpen = _unlockedPoseToOpen.asStateFlow()

    private val heroUnlock = MutableStateFlow(HeroUnlock())

    private var heroPoseId: Int? = null
    private var heroShownAt: TimeMark? = null

    val uiState: StateFlow<HomeUiState> = combine(
        posesWithRotatingHero(),
        captureUseCase.observeCaptures(),
        poseLockUseCase.observeAccess(),
        heroUnlock
    ) { posesWithHero, captures, access, unlock ->
        contentOf(posesWithHero, captures, access, unlock)
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(SUBSCRIPTION_TIMEOUT),
        HomeUiState.Loading
    )

    fun importPose(title: String, sourceUri: String) {
        viewModelScope.launch {
            val imported = poseLibraryUseCase.importPose(title, sourceUri)
            if (imported == null) _importFailed.value = true else _importedPose.value = imported
        }
    }

    fun consumeImportedPose() {
        _importedPose.value = null
    }

    fun consumeImportFailure() {
        _importFailed.value = false
    }

    fun showLockedPose(pose: Pose, target: HomeUnlockTarget) {
        heroUnlock.value = HeroUnlock(lockedPose = pose, target = target)
    }

    fun dismissLockedPose() {
        if (heroUnlock.value.isAdLoading) return
        heroUnlock.value = HeroUnlock()
    }

    fun onUnlockAdStarted() {
        heroUnlock.value = heroUnlock.value.copy(isAdLoading = true)
    }

    fun onUnlockAdShown() {
        heroUnlock.value = HeroUnlock()
    }

    fun onUnlockAdFinished(pose: Pose, target: HomeUnlockTarget, wasRewarded: Boolean) {
        heroUnlock.value = heroUnlock.value.copy(isAdLoading = false)
        if (!wasRewarded) return
        viewModelScope.launch {
            poseLockUseCase.unlockWithRewardedAd(pose.id)
            heroUnlock.value = HeroUnlock()
            _unlockedPoseToOpen.value = HomeUnlockedPose(poseId = pose.id, target = target)
        }
    }

    fun consumeUnlockedPose() {
        _unlockedPoseToOpen.value = null
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun posesWithRotatingHero(): Flow<PosesWithHero> =
        poseLibraryUseCase.observePoses().transformLatest { poses ->
            while (true) {
                val hero = currentOrNextHero(poses)
                emit(PosesWithHero(poses, hero))
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
        unlock: HeroUnlock
    ): HomeUiState.Content {
        val poses = posesWithHero.poses
        val lockedIds = access.lockedIdsIn(poses)
        return HomeUiState.Content(
            hero = posesWithHero.hero?.let { pose ->
                HomeHero(pose = pose, isLocked = pose.id in lockedIds)
            },
            quickActions = HomeQuickActionId.entries.map { id ->
                HomeQuickAction(id = id, count = poses.countFor(id))
            },
            progress = captureProgressUseCase.progressOf(captures),
            poseOfTheDay = poseLibraryUseCase.poseOfTheDay(poses)?.let { pose ->
                HomeDailyPose(pose = pose, isLocked = pose.id in lockedIds)
            },
            lockedPose = unlock.lockedPose,
            unlockTarget = unlock.target,
            isUnlockAdLoading = unlock.isAdLoading
        )
    }

    private fun List<Pose>.countFor(id: HomeQuickActionId): Int = when {
        id.category != null -> count { it.category == id.category }
        id == HomeQuickActionId.Explore -> size
        else -> 0
    }

    private companion object {
        const val SUBSCRIPTION_TIMEOUT = 5_000L
        val HERO_ROTATION_INTERVAL = 1.minutes
    }
}
