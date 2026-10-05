package com.aipose.camera.posematch.ui.vm

import android.content.Context
import android.text.format.DateFormat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aipose.camera.posematch.R
import com.aipose.camera.posematch.domain.models.Capture
import com.aipose.camera.posematch.domain.models.PERFECT_MATCH_SCORE
import com.aipose.camera.posematch.domain.usecase.CaptureProgressUseCase
import com.aipose.camera.posematch.domain.usecase.CaptureUseCase
import com.aipose.camera.posematch.ui.screens.collections.models.CaptureAlbum
import com.aipose.camera.posematch.ui.screens.collections.models.CaptureUi
import com.aipose.camera.posematch.ui.screens.collections.models.CollectionsFilter
import com.aipose.camera.posematch.ui.screens.collections.models.CollectionsSort
import com.aipose.camera.posematch.ui.screens.collections.models.CollectionsStats
import com.aipose.camera.posematch.ui.screens.collections.models.CollectionsUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CollectionsViewModel(
    private val context: Context,
    private val captureUseCase: CaptureUseCase,
    private val captureProgressUseCase: CaptureProgressUseCase
) : ViewModel() {

    private val _query = MutableStateFlow("")
    val query = _query.asStateFlow()

    private val captures: StateFlow<List<CaptureUi>> = captureUseCase.observeCaptures()
        .map { items -> items.map { capture -> capture.toUi() } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(SUBSCRIPTION_TIMEOUT), emptyList())

    private val _filter = MutableStateFlow(CollectionsFilter.All)
    private val _sort = MutableStateFlow(CollectionsSort.Newest)
    private val _isSortSheetVisible = MutableStateFlow(false)

    val uiState: StateFlow<CollectionsUiState> = combine(
        captures,
        _query,
        _filter,
        _sort,
        _isSortSheetVisible,
    ) { items, query, filter, sort, isSortSheetVisible ->
        val domainCaptures = items.map { it.capture }
        CollectionsUiState.Content(
            totalCount = items.size,
            stats = items.toStats(),
            albums = items
                .filter { it.matches(query) && it.matches(filter) }
                .sortedWith(sort.comparator())
                .toAlbums(),
            filter = filter,
            sort = sort,
            recentPerfectShots = captureProgressUseCase.perfectShotsWithinDays(domainCaptures, RECENT_DAYS),
            isSortSheetVisible = isSortSheetVisible,
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(SUBSCRIPTION_TIMEOUT),
        CollectionsUiState.Loading
    )

    private val captureFlows = mutableMapOf<Long, StateFlow<CaptureUi?>>()

    fun captureFor(captureId: Long): StateFlow<CaptureUi?> =
        captureFlows.getOrPut(captureId) {
            captures
                .map { items -> items.firstOrNull { it.id == captureId } }
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

    fun showSortSheet() {
        _isSortSheetVisible.value = true
    }

    fun dismissSortSheet() {
        _isSortSheetVisible.value = false
    }

    fun selectSort(sort: CollectionsSort) {
        _sort.value = sort
        _isSortSheetVisible.value = false
    }

    private fun CaptureUi.matches(filter: CollectionsFilter): Boolean = when (filter) {
        CollectionsFilter.All -> true
        CollectionsFilter.TopMatch -> capture.matchScore >= PERFECT_MATCH_SCORE
        CollectionsFilter.Recent -> captureProgressUseCase.isWithinDays(capture, RECENT_DAYS)
    }

    private fun CollectionsSort.comparator(): Comparator<CaptureUi> = when (this) {
        CollectionsSort.Newest -> compareByDescending { it.capture.capturedAtMillis }
        CollectionsSort.HighestMatch -> compareByDescending { it.capture.matchScore }
        CollectionsSort.NameAsc -> compareBy(String.CASE_INSENSITIVE_ORDER) { it.titleLabel }
    }

    private fun List<CaptureUi>.toStats(): CollectionsStats = CollectionsStats(
        shots = size,
        places = map { it.locationLabel }.distinct().size,
        averageMatch = if (isEmpty()) 0 else sumOf { it.capture.matchScore } / size,
    )

    fun toggleFavorite(capture: Capture) {
        viewModelScope.launch { captureUseCase.toggleFavorite(capture) }
    }

    fun delete(capture: Capture) {
        viewModelScope.launch { captureUseCase.delete(capture) }
    }

    private fun Capture.toUi(): CaptureUi = CaptureUi(
        capture = this,
        formattedDate = format(LONG_DATE_PATTERN, capturedAtMillis),
        capturedLabel = capturedLabel(),
        locationLabel = location.name ?: context.getString(R.string.unknown_location),
        titleLabel = title.ifBlank { context.getString(R.string.capture_untitled) }
    )

    private fun Capture.capturedLabel(): String {
        val date = Date(capturedAtMillis)
        val time = DateFormat.getTimeFormat(context).format(date)
        return when (captureProgressUseCase.daysSince(this)) {
            0 -> context.getString(R.string.detail_today, time)
            1 -> context.getString(R.string.detail_yesterday, time)
            else -> context.getString(R.string.detail_date_time, DateFormat.getMediumDateFormat(context).format(date), time)
        }
    }

    private fun format(pattern: String, millis: Long): String =
        SimpleDateFormat(pattern, Locale.getDefault()).format(Date(millis))

    private fun List<CaptureUi>.toAlbums(): List<CaptureAlbum> = groupBy { it.locationLabel }
        .map { (locationLabel, captures) -> CaptureAlbum(locationLabel, captures) }

    private companion object {
        const val LONG_DATE_PATTERN = "MMM dd, yyyy hh:mm a"
        const val SUBSCRIPTION_TIMEOUT = 5_000L
        const val RECENT_DAYS = 7
    }
}
