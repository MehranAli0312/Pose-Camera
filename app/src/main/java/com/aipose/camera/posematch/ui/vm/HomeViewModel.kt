package com.aipose.camera.posematch.ui.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aipose.camera.posematch.domain.models.Capture
import com.aipose.camera.posematch.domain.models.Pose
import com.aipose.camera.posematch.domain.usecase.CaptureProgressUseCase
import com.aipose.camera.posematch.domain.usecase.CaptureUseCase
import com.aipose.camera.posematch.domain.usecase.FavoritePoseUseCase
import com.aipose.camera.posematch.domain.usecase.PoseLibraryUseCase
import com.aipose.camera.posematch.ui.models.PoseCategories
import com.aipose.camera.posematch.ui.screens.home.models.HomeFilter
import com.aipose.camera.posematch.ui.screens.home.models.HomeHero
import com.aipose.camera.posematch.ui.screens.home.models.HomeQuickAction
import com.aipose.camera.posematch.ui.screens.home.models.HomeQuickActionId
import com.aipose.camera.posematch.ui.screens.home.models.HomeUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

private data class FilterSelection(
    val category: String? = null,
    val difficulty: String? = null
)

class HomeViewModel(
    private val poseLibraryUseCase: PoseLibraryUseCase,
    private val captureUseCase: CaptureUseCase,
    private val captureProgressUseCase: CaptureProgressUseCase,
    private val favoritePoseUseCase: FavoritePoseUseCase
) : ViewModel() {

    val savedPoseIds: StateFlow<Set<Int>> = favoritePoseUseCase.observeFavorites()
        .map { favorites -> favorites.keys }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(SUBSCRIPTION_TIMEOUT), emptySet())

    fun toggleSavedPose(pose: Pose) {
        viewModelScope.launch {
            favoritePoseUseCase.setFavorite(pose.id, pose.id !in savedPoseIds.value)
        }
    }

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _importedPose = MutableStateFlow<Pose?>(null)
    val importedPose = _importedPose.asStateFlow()

    private val _importFailed = MutableStateFlow(false)
    val importFailed = _importFailed.asStateFlow()

    private val filterSelection = MutableStateFlow(FilterSelection())

    val uiState: StateFlow<HomeUiState> = combine(
        poseLibraryUseCase.observePoses(),
        captureUseCase.observeCaptures(),
        _searchQuery,
        filterSelection
    ) { poses, captures, query, selection ->
        contentOf(poses, captures, query, selection)
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

    fun selectCategory(category: String) {
        filterSelection.value = filterSelection.value.copy(category = category)
    }

    fun selectDifficulty(difficulty: String) {
        filterSelection.value = filterSelection.value.copy(difficulty = difficulty)
    }

    fun shuffle() {
        val content = uiState.value as? HomeUiState.Content ?: return
        val category = content.filter.categories.randomOrNull() ?: return
        filterSelection.value = FilterSelection(category = category)
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

    private fun contentOf(
        poses: List<Pose>,
        captures: List<Capture>,
        query: String,
        selection: FilterSelection
    ): HomeUiState.Content {
        val categories = poses.orderedCategories()
        val category = selection.category?.takeIf { it in categories } ?: categories.firstOrNull()
        val inCategory = poses.filter { category == null || it.category == category }
        val difficulties = inCategory.map { it.difficulty }.distinct()
        val difficulty = selection.difficulty?.takeIf { it in difficulties }
            ?: difficulties.firstOrNull()
        val heroPose = inCategory.firstOrNull { difficulty == null || it.difficulty == difficulty }
            ?: poses.firstOrNull()
        return HomeUiState.Content(
            filter = HomeFilter(
                categories = categories,
                selectedCategory = category,
                difficulties = difficulties,
                selectedDifficulty = difficulty
            ),
            hero = heroPose?.let { pose ->
                HomeHero(
                    pose = pose,
                    categoryCount = poses.count { it.category == pose.category },
                    bestMatch = captureProgressUseCase.bestMatchFor(captures, pose.id)
                )
            },
            quickActions = HomeQuickActionId.entries.map { id ->
                HomeQuickAction(id = id, count = poses.countFor(id))
            },
            progress = captureProgressUseCase.progressOf(captures),
            poseOfTheDay = poseLibraryUseCase.poseOfTheDay(poses),
            searchResults = if (query.isBlank()) null else poses.filter { it.matches(query) }
        )
    }

    private fun List<Pose>.countFor(id: HomeQuickActionId): Int = when {
        id.category != null -> count { it.category == id.category }
        id == HomeQuickActionId.Explore -> size
        else -> 0
    }

    private fun List<Pose>.orderedCategories(): List<String> {
        val present = map { it.category }.distinct()
        return PoseCategories.displayOrder.filter { it in present } +
            present.filter { it !in PoseCategories.displayOrder }
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
