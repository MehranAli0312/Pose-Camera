package com.aipose.camera.posematch.ui.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aipose.camera.posematch.domain.models.Pose
import com.aipose.camera.posematch.domain.usecase.PoseLibraryUseCase
import com.aipose.camera.posematch.ui.screens.home.data.PoseCategories
import com.aipose.camera.posematch.ui.screens.home.models.HomeUiState
import com.aipose.camera.posematch.ui.screens.home.models.PoseCategorySection
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HomeViewModel(private val poseLibraryUseCase: PoseLibraryUseCase) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _importedPose = MutableStateFlow<Pose?>(null)
    val importedPose = _importedPose.asStateFlow()

    private val _importFailed = MutableStateFlow(false)
    val importFailed = _importFailed.asStateFlow()

    val uiState: StateFlow<HomeUiState> = combine(
        poseLibraryUseCase.observePoses(),
        _searchQuery
    ) { poses, query ->
        HomeUiState.Content(
            heroPose = poses.heroPose(),
            sections = poses.toSections(),
            searchResults = if (query.isBlank()) null else poses.filter { it.matches(query) }
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(SUBSCRIPTION_TIMEOUT),
        HomeUiState.Loading
    )

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun clearSearch() {
        _searchQuery.value = ""
    }

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

    private fun List<Pose>.heroPose(): Pose? =
        firstOrNull { it.category == PoseCategories.COUPLE } ?: firstOrNull()

    private fun List<Pose>.toSections(): List<PoseCategorySection> {
        val grouped = groupBy { it.category }
        val present = grouped.keys
        val ordered = PoseCategories.displayOrder.filter { it in present } +
            present.filter { it !in PoseCategories.displayOrder }
        return ordered.mapNotNull { category ->
            val poses = grouped[category].orEmpty()
            if (poses.isEmpty()) {
                null
            } else {
                PoseCategorySection(
                    category = category,
                    previewPoses = poses.take(SECTION_PREVIEW_SIZE),
                    totalCount = poses.size
                )
            }
        }
    }

    private fun Pose.matches(query: String): Boolean {
        val normalized = query.trim()
        if (normalized.isEmpty()) return true
        return title.contains(normalized, ignoreCase = true) ||
            description.contains(normalized, ignoreCase = true) ||
            category.contains(normalized, ignoreCase = true) ||
            tags.any { tag -> tag.contains(normalized, ignoreCase = true) }
    }

    private companion object {
        const val SECTION_PREVIEW_SIZE = 3
        const val SUBSCRIPTION_TIMEOUT = 5_000L
    }
}
