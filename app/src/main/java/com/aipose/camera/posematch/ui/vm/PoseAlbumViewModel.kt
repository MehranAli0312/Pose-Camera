package com.aipose.camera.posematch.ui.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aipose.camera.posematch.domain.models.Pose
import com.aipose.camera.posematch.domain.usecase.FavoritePoseUseCase
import com.aipose.camera.posematch.domain.usecase.PoseLibraryUseCase
import com.aipose.camera.posematch.ui.graph.NavRoute
import com.aipose.camera.posematch.ui.models.PoseCategories
import com.aipose.camera.posematch.ui.screens.poseAlbum.models.PoseAlbumUiState
import com.aipose.camera.posematch.ui.screens.poseAlbum.models.PoseCategoryCount
import com.aipose.camera.posematch.ui.screens.poseAlbum.models.PoseDifficulty
import com.aipose.camera.posematch.ui.screens.poseAlbum.models.PoseSort
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

private data class ExploreControls(
    val category: String? = null,
    val sort: PoseSort = PoseSort.Featured,
    val isSortSheetVisible: Boolean = false
)

class PoseAlbumViewModel(
    private val poseLibraryUseCase: PoseLibraryUseCase,
    private val favoritePoseUseCase: FavoritePoseUseCase
) : ViewModel() {

    private val _query = MutableStateFlow("")
    val query = _query.asStateFlow()

    private val controls = MutableStateFlow(ExploreControls())
    private var requestedCategory: String? = null

    private val savedPoseIds: StateFlow<Set<Int>> = favoritePoseUseCase.observeFavorites()
        .map { favorites -> favorites.keys }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(SUBSCRIPTION_TIMEOUT), emptySet())

    val uiState: StateFlow<PoseAlbumUiState> = combine(
        poseLibraryUseCase.observePoses(),
        savedPoseIds,
        _query,
        controls,
    ) { poses, saved, query, controls ->
        val visible = poses
            .filter { pose -> controls.category == null || pose.category == controls.category }
            .filter { pose -> pose.matches(query) }
        PoseAlbumUiState.Content(
            totalCount = poses.size,
            categories = poses.categoryCounts(),
            selectedCategory = controls.category,
            poses = visible.sortedBy(controls.sort),
            savedPoseIds = saved,
            sort = controls.sort,
            isSortSheetVisible = controls.isSortSheetVisible,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(SUBSCRIPTION_TIMEOUT), PoseAlbumUiState.Loading)

    fun onCategoryRequested(category: String) {
        if (requestedCategory == category) return
        requestedCategory = category
        val selected = category.takeUnless { it == NavRoute.PoseAlbumScreenRoute.ALL_CATEGORIES }
        controls.value = controls.value.copy(category = selected)
    }

    fun selectCategory(category: String?) {
        controls.value = controls.value.copy(category = category)
    }

    fun setQuery(query: String) {
        _query.value = query
    }

    fun showSortSheet() {
        controls.value = controls.value.copy(isSortSheetVisible = true)
    }

    fun dismissSortSheet() {
        controls.value = controls.value.copy(isSortSheetVisible = false)
    }

    fun selectSort(sort: PoseSort) {
        controls.value = controls.value.copy(sort = sort, isSortSheetVisible = false)
    }

    fun toggleSaved(pose: Pose) {
        viewModelScope.launch {
            favoritePoseUseCase.setFavorite(pose.id, pose.id !in savedPoseIds.value)
        }
    }

    private fun List<Pose>.categoryCounts(): List<PoseCategoryCount> {
        val counts = groupingBy { it.category }.eachCount()
        val ordered = PoseCategories.displayOrder.filter { it in counts } +
            counts.keys.filter { it !in PoseCategories.displayOrder }
        return ordered.map { category -> PoseCategoryCount(category, counts.getValue(category)) }
    }

    private fun List<Pose>.sortedBy(sort: PoseSort): List<Pose> = when (sort) {
        PoseSort.Featured -> this
        PoseSort.NameAsc -> sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.title })
        PoseSort.EasiestFirst -> sortedBy { PoseDifficulty.fromKey(it.difficulty)?.ordinal ?: Int.MAX_VALUE }
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
        const val SUBSCRIPTION_TIMEOUT = 5_000L
    }
}
