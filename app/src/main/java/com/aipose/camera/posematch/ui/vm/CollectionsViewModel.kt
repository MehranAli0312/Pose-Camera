package com.aipose.camera.posematch.ui.vm

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aipose.camera.posematch.R
import com.aipose.camera.posematch.domain.models.Capture
import com.aipose.camera.posematch.domain.usecase.CaptureUseCase
import com.aipose.camera.posematch.ui.screens.collections.models.CaptureAlbum
import com.aipose.camera.posematch.ui.screens.collections.models.CaptureUi
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
    private val captureUseCase: CaptureUseCase
) : ViewModel() {

    private val _query = MutableStateFlow("")
    val query = _query.asStateFlow()

    private val captures: StateFlow<List<CaptureUi>> = captureUseCase.observeCaptures()
        .map { items -> items.map { capture -> capture.toUi() } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(SUBSCRIPTION_TIMEOUT), emptyList())

    val uiState: StateFlow<CollectionsUiState> = combine(captures, _query) { items, query ->
        CollectionsUiState.Content(
            totalCount = items.size,
            albums = items.filter { it.matches(query) }.toAlbums()
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(SUBSCRIPTION_TIMEOUT),
        CollectionsUiState.Loading
    )

    private val albumFlows = mutableMapOf<String, StateFlow<CaptureAlbum>>()
    private val captureFlows = mutableMapOf<Long, StateFlow<CaptureUi?>>()

    fun albumFor(locationLabel: String): StateFlow<CaptureAlbum> =
        albumFlows.getOrPut(locationLabel) {
            captures
                .map { items ->
                    CaptureAlbum(
                        locationLabel = locationLabel,
                        captures = items.filter { it.locationLabel == locationLabel }
                    )
                }
                .stateIn(
                    viewModelScope,
                    SharingStarted.WhileSubscribed(SUBSCRIPTION_TIMEOUT),
                    CaptureAlbum(locationLabel, emptyList())
                )
        }

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

    fun toggleFavorite(capture: Capture) {
        viewModelScope.launch { captureUseCase.toggleFavorite(capture) }
    }

    fun delete(capture: Capture) {
        viewModelScope.launch { captureUseCase.delete(capture) }
    }

    private fun Capture.toUi(): CaptureUi = CaptureUi(
        capture = this,
        formattedDate = format(LONG_DATE_PATTERN, capturedAtMillis),
        formattedShortDate = format(SHORT_DATE_PATTERN, capturedAtMillis),
        locationLabel = location.name ?: context.getString(R.string.unknown_location),
        titleLabel = title.ifBlank { context.getString(R.string.capture_untitled) }
    )

    private fun format(pattern: String, millis: Long): String =
        SimpleDateFormat(pattern, Locale.getDefault()).format(Date(millis))

    private fun List<CaptureUi>.toAlbums(): List<CaptureAlbum> = groupBy { it.locationLabel }
        .map { (locationLabel, captures) -> CaptureAlbum(locationLabel, captures) }

    private companion object {
        const val LONG_DATE_PATTERN = "MMM dd, yyyy hh:mm a"
        const val SHORT_DATE_PATTERN = "MMM dd - hh:mm a"
        const val SUBSCRIPTION_TIMEOUT = 5_000L
    }
}
