package com.aipose.camera.posematch.ui.vm

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aipose.camera.posematch.R
import com.aipose.camera.posematch.domain.models.Capture
import com.aipose.camera.posematch.domain.models.PERFECT_MATCH_SCORE
import com.aipose.camera.posematch.domain.usecase.CaptureProgressUseCase
import com.aipose.camera.posematch.domain.usecase.CaptureUseCase
import com.aipose.camera.posematch.ui.screens.captureAlbum.models.CaptureAlbumUiState
import com.aipose.camera.posematch.ui.screens.collections.models.CollectionsFilter
import com.aipose.camera.posematch.ui.screens.collections.models.CollectionsSort
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CaptureAlbumViewModel(
    private val context: Context,
    private val captureUseCase: CaptureUseCase,
    private val captureProgressUseCase: CaptureProgressUseCase
) : ViewModel() {

    private val _locationLabel = MutableStateFlow<String?>(null)
    private val _filter = MutableStateFlow(CollectionsFilter.All)
    private val _sort = MutableStateFlow(CollectionsSort.Newest)
    private val _isRemoveDialogVisible = MutableStateFlow(false)
    private val _isSortSheetVisible = MutableStateFlow(false)

    private data class AlbumControls(
        val filter: CollectionsFilter,
        val sort: CollectionsSort,
        val isRemoveDialogVisible: Boolean,
        val isSortSheetVisible: Boolean
    )

    private val controls = combine(
        _filter,
        _sort,
        _isRemoveDialogVisible,
        _isSortSheetVisible,
        ::AlbumControls,
    )

    val uiState: StateFlow<CaptureAlbumUiState> = combine(
        captureUseCase.observeCaptures(),
        _locationLabel,
        controls,
    ) { captures, locationLabel, controls ->
        if (locationLabel == null) return@combine CaptureAlbumUiState.Loading
        val albumCaptures = captures.filter { it.locationLabel() == locationLabel }
        if (albumCaptures.isEmpty()) return@combine CaptureAlbumUiState.Removed
        CaptureAlbumUiState.Content(
            locationLabel = locationLabel,
            shots = albumCaptures
                .filter { it.matches(controls.filter) }
                .sortedWith(controls.sort.comparator()),
            totalCount = albumCaptures.size,
            averageMatch = albumCaptures.sumOf { it.matchScore } / albumCaptures.size,
            bestMatch = albumCaptures.maxOf { it.matchScore },
            lastShotDaysAgo = albumCaptures.minOf { captureProgressUseCase.daysSince(it) },
            filter = controls.filter,
            sort = controls.sort,
            isRemoveDialogVisible = controls.isRemoveDialogVisible,
            isSortSheetVisible = controls.isSortSheetVisible,
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(SUBSCRIPTION_TIMEOUT),
        CaptureAlbumUiState.Loading,
    )

    fun onAlbumRequested(locationLabel: String) {
        _locationLabel.value = locationLabel
    }

    fun selectFilter(filter: CollectionsFilter) {
        _filter.value = filter
    }

    fun selectSort(sort: CollectionsSort) {
        _sort.value = sort
        _isSortSheetVisible.value = false
    }

    fun showSortSheet() {
        _isSortSheetVisible.value = true
    }

    fun dismissSortSheet() {
        _isSortSheetVisible.value = false
    }

    fun showRemoveDialog() {
        _isRemoveDialogVisible.value = true
    }

    fun dismissRemoveDialog() {
        _isRemoveDialogVisible.value = false
    }

    fun removeAll() {
        val locationLabel = _locationLabel.value ?: return
        _isRemoveDialogVisible.value = false
        viewModelScope.launch {
            val captures = captureUseCase.observeCaptures().first()
            captureUseCase.deleteAll(captures.filter { it.locationLabel() == locationLabel })
        }
    }

    private fun CollectionsSort.comparator(): Comparator<Capture> = when (this) {
        CollectionsSort.Newest -> compareByDescending { it.capturedAtMillis }
        CollectionsSort.HighestMatch -> compareByDescending { it.matchScore }
        CollectionsSort.NameAsc -> compareBy(String.CASE_INSENSITIVE_ORDER) { it.title }
    }

    private fun Capture.locationLabel(): String =
        location.name ?: context.getString(R.string.unknown_location)

    private fun Capture.matches(filter: CollectionsFilter): Boolean = when (filter) {
        CollectionsFilter.All -> true
        CollectionsFilter.TopMatch -> matchScore >= PERFECT_MATCH_SCORE
        CollectionsFilter.Recent -> captureProgressUseCase.isWithinDays(this, RECENT_DAYS)
    }

    private companion object {
        const val SUBSCRIPTION_TIMEOUT = 5_000L
        const val RECENT_DAYS = 7
    }
}
