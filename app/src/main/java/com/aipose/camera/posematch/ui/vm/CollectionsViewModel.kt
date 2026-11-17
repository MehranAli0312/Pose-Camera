package com.aipose.camera.posematch.ui.vm

import android.content.Context
import android.text.format.DateFormat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aipose.camera.posematch.R
import com.aipose.camera.posematch.domain.models.AnalyticsEvent
import com.aipose.camera.posematch.domain.models.Capture
import com.aipose.camera.posematch.domain.models.PERFECT_MATCH_SCORE
import com.aipose.camera.posematch.domain.models.Pose
import com.aipose.camera.posematch.domain.usecase.CaptureProgressUseCase
import com.aipose.camera.posematch.domain.usecase.CaptureUseCase
import com.aipose.camera.posematch.domain.usecase.FavoritePoseUseCase
import com.aipose.camera.posematch.domain.usecase.PoseLibraryUseCase
import com.aipose.camera.posematch.domain.usecase.PoseLockUseCase
import com.aipose.camera.posematch.domain.usecase.TrackEventUseCase
import com.aipose.camera.posematch.ui.screens.collections.models.CaptureAlbum
import com.aipose.camera.posematch.ui.screens.collections.models.CaptureUi
import com.aipose.camera.posematch.ui.screens.collections.models.CollectionsFilter
import com.aipose.camera.posematch.ui.screens.collections.models.CollectionsUiState
import com.aipose.camera.posematch.ui.screens.collections.models.FavoritePose
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

private data class CollectionsControls(
    val query: String,
    val filter: CollectionsFilter,
)

class CollectionsViewModel(
    private val context: Context,
    private val captureUseCase: CaptureUseCase,
    private val captureProgressUseCase: CaptureProgressUseCase,
    private val favoritePoseUseCase: FavoritePoseUseCase,
    private val poseLockUseCase: PoseLockUseCase,
    poseLibraryUseCase: PoseLibraryUseCase,
    private val trackEventUseCase: TrackEventUseCase,
) : ViewModel() {

    private val _query = MutableStateFlow("")
    val query = _query.asStateFlow()

    private val captures: StateFlow<List<CaptureUi>?> = captureUseCase.observeCaptures()
        .map { items -> items.map { capture -> capture.toUi() } }
        .flowOn(Dispatchers.Default)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(SUBSCRIPTION_TIMEOUT), null)

    private val _filter = MutableStateFlow(CollectionsFilter.All)
    private val _lockedPose = MutableStateFlow<Pose?>(null)
    val lockedPose = _lockedPose.asStateFlow()

    private val _unlockedPoseToOpen = MutableStateFlow<Int?>(null)
    val unlockedPoseToOpen = _unlockedPoseToOpen.asStateFlow()

    private val controls = combine(
        _query,
        _filter,
    ) { query, filter ->
        CollectionsControls(query, filter)
    }

    private val favoritePoses = combine(
        poseLibraryUseCase.observePoses(),
        poseLockUseCase.observeAccess(),
        favoritePoseUseCase.observeFavorites(),
    ) { poses, access, favorites ->
        val lockedIds = access.lockedIdsIn(poses)
        poses.mapNotNull { pose ->
            favorites[pose.id]?.let { savedAt ->
                FavoritePose(
                    pose = pose,
                    savedAtMillis = savedAt,
                    isLocked = pose.id in lockedIds,
                )
            }
        }
    }

    val uiState: StateFlow<CollectionsUiState> = combine(
        captures,
        favoritePoses,
        controls,
    ) { items, favorites, controls ->
        if (items == null) return@combine CollectionsUiState.Loading
        val domainCaptures = items.map { item -> item.capture }
        CollectionsUiState.Content(
            albums = items
                .filter { item -> item.matches(controls.query) && item.matches(controls.filter) }
                .sortedByDescending { item -> item.capture.capturedAtMillis }
                .toAlbums(),
            favoritePoses = favorites
                .filter { favorite -> favorite.pose.matches(controls.query) }
                .sortedByDescending { favorite -> favorite.savedAtMillis },
            filter = controls.filter,
            recentPerfectShots = captureProgressUseCase.perfectShotsWithinDays(
                domainCaptures,
                RECENT_DAYS,
            ),
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(SUBSCRIPTION_TIMEOUT),
        CollectionsUiState.Loading,
    )

    private val captureFlows = mutableMapOf<Long, StateFlow<CaptureUi?>>()

    fun captureFor(captureId: Long): StateFlow<CaptureUi?> =
        captureFlows.getOrPut(captureId) {
            captures
                .map { items -> items?.firstOrNull { item -> item.id == captureId } }
                .stateIn(viewModelScope, SharingStarted.WhileSubscribed(SUBSCRIPTION_TIMEOUT), null)
        }

    fun setQuery(query: String) {
        _query.value = query
    }

    fun clearQuery() {
        _query.value = ""
    }

    fun selectFilter(filter: CollectionsFilter) {
        _filter.value = filter
    }

    fun removeFavoritePose(favorite: FavoritePose) {
        viewModelScope.launch { favoritePoseUseCase.setFavorite(favorite.pose.id, false) }
    }

    fun showLockedPose(favorite: FavoritePose) {
        _lockedPose.value = favorite.pose
    }

    fun dismissLockedPose() {
        _lockedPose.value = null
    }

    fun unlockAfterRewardedAd(pose: Pose) {
        _lockedPose.value = null
        viewModelScope.launch {
            poseLockUseCase.unlockWithRewardedAd(pose.id)
            _unlockedPoseToOpen.value = pose.id
        }
    }

    fun consumeUnlockedPose() {
        _unlockedPoseToOpen.value = null
    }

    fun trackEvent(event: AnalyticsEvent) {
        viewModelScope.launch { trackEventUseCase.track(event) }
    }

    fun delete(capture: Capture) {
        viewModelScope.launch { captureUseCase.delete(capture) }
        trackEvent(AnalyticsEvent.CaptureDeleted)
    }

    private fun CaptureUi.matches(filter: CollectionsFilter): Boolean = when (filter) {
        CollectionsFilter.All -> true
        CollectionsFilter.TopMatch -> capture.matchScore >= PERFECT_MATCH_SCORE
        CollectionsFilter.Recent -> captureProgressUseCase.isWithinDays(capture, RECENT_DAYS)
        CollectionsFilter.Favorite -> false
    }

    private fun Pose.matches(query: String): Boolean {
        val normalized = query.trim()
        if (normalized.isEmpty()) return true
        return title.contains(normalized, ignoreCase = true) ||
            description.contains(normalized, ignoreCase = true) ||
            category.contains(normalized, ignoreCase = true) ||
            tags.any { tag -> tag.contains(normalized, ignoreCase = true) }
    }

    private fun Capture.toUi(): CaptureUi = CaptureUi(
        capture = this,
        formattedDate = format(LONG_DATE_PATTERN, capturedAtMillis),
        capturedLabel = capturedLabel(),
        locationLabel = location.name ?: context.getString(R.string.unknown_location),
        titleLabel = title.ifBlank { context.getString(R.string.capture_untitled) },
    )

    private fun Capture.capturedLabel(): String {
        val date = Date(capturedAtMillis)
        val time = DateFormat.getTimeFormat(context).format(date)
        return when (captureProgressUseCase.daysSince(this)) {
            0 -> context.getString(R.string.detail_today, time)
            1 -> context.getString(R.string.detail_yesterday, time)
            else -> context.getString(
                R.string.detail_date_time,
                DateFormat.getMediumDateFormat(context).format(date),
                time,
            )
        }
    }

    private fun format(pattern: String, millis: Long): String =
        SimpleDateFormat(pattern, Locale.getDefault()).format(Date(millis))

    private fun List<CaptureUi>.toAlbums(): List<CaptureAlbum> = groupBy { item -> item.locationLabel }
        .map { (locationLabel, captures) -> CaptureAlbum(locationLabel, captures) }

    private companion object {
        const val LONG_DATE_PATTERN = "MMM dd, yyyy hh:mm a"
        const val SUBSCRIPTION_TIMEOUT = 5_000L
        const val RECENT_DAYS = 7
    }
}
