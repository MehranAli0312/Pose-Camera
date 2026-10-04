package com.aipose.camera.posematch.ui.viewmodel

import android.app.Application
import android.graphics.PointF
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.aipose.camera.posematch.data.*
import com.aipose.camera.posematch.data.repository.IAppRepository
import com.aipose.camera.posematch.domain.PoseMatchAnalyzer
import com.aipose.camera.posematch.domain.PoseMatchState
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.flow.*
import kotlin.math.roundToInt
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Shared history date format, also used for search matching. */
fun formatHistoryDate(timestamp: Long): String =
    SimpleDateFormat("MMM dd, yyyy hh:mm a", Locale.getDefault()).format(Date(timestamp))

class MainViewModel(
    application: Application,
    private val repository: IAppRepository
) : AndroidViewModel(application) {

    // Preferences & Config Flows
    val onboardingCompleted: StateFlow<Boolean> = repository.onboardingCompleted
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val appTheme: StateFlow<String> = repository.appTheme
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "Dark")

    val retainSkeleton: StateFlow<Boolean> = repository.retainSkeleton
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    // Default & Custom Poses
    private val _defaultPoses = MutableStateFlow<List<PoseItem>>(emptyList())
    val defaultPoses: StateFlow<List<PoseItem>> = _defaultPoses.asStateFlow()

    val customPoses: StateFlow<List<CustomPose>> = repository.getCustomPoses()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Historical captures
    val capturedHistory: StateFlow<List<CapturedPhoto>> = repository.getCapturedHistory()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // History search (matches location, title, category, score, or date/time)
    private val _historyQuery = MutableStateFlow("")
    val historyQuery = _historyQuery.asStateFlow()

    val filteredHistory: StateFlow<List<CapturedPhoto>> = combine(
        capturedHistory, _historyQuery
    ) { list, query ->
        if (query.isBlank()) list
        else {
            val q = query.trim().lowercase()
            list.filter { photo ->
                photo.locationName.lowercase().contains(q) ||
                    photo.title.lowercase().contains(q) ||
                    photo.category.lowercase().contains(q) ||
                    photo.matchScore.toString().contains(q) ||
                    formatHistoryDate(photo.dateTimestamp).lowercase().contains(q)
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setHistoryQuery(query: String) { _historyQuery.value = query }

    // Search & Filter State
    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow("All")
    val selectedCategory = _selectedCategory.asStateFlow()

    // Filtered default poses
    val filteredPoses: StateFlow<List<PoseItem>> = combine(
        defaultPoses, _searchQuery, _selectedCategory
    ) { poses, query, category ->
        poses.filter { pose ->
            val matchesSearch = query.isEmpty() ||
                    pose.title.contains(query, ignoreCase = true) ||
                    pose.description.contains(query, ignoreCase = true) ||
                    pose.tags.any { it.contains(query, ignoreCase = true) }
            val matchesCategory = category == "All" || pose.category.equals(category, ignoreCase = true)
            matchesSearch && matchesCategory
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Camera matching states
    private val _selectedPose = MutableStateFlow<PoseItem?>(null)
    val selectedPose = _selectedPose.asStateFlow()

    // Multitouch gestures for references
    private val _poseOpacity = MutableStateFlow(0.45f)
    val poseOpacity = _poseOpacity.asStateFlow()

    private val _poseScale = MutableStateFlow(1.0f)
    val poseScale = _poseScale.asStateFlow()

    private val _poseOffsetX = MutableStateFlow(0f)
    val poseOffsetX = _poseOffsetX.asStateFlow()

    private val _poseOffsetY = MutableStateFlow(0f)
    val poseOffsetY = _poseOffsetY.asStateFlow()

    private val _poseRotation = MutableStateFlow(0f)
    val poseRotation = _poseRotation.asStateFlow()

    // Pro Camera Manual Overrides
    private val _cameraFlashMode = MutableStateFlow("Off")
    val cameraFlashMode = _cameraFlashMode.asStateFlow()

    private val _cameraFacing = MutableStateFlow("Back")
    val cameraFacing = _cameraFacing.asStateFlow()

    private val _cameraTimer = MutableStateFlow(0)
    val cameraTimer = _cameraTimer.asStateFlow()

    private val _cameraGridVisible = MutableStateFlow(false)
    val cameraGridVisible = _cameraGridVisible.asStateFlow()

    private val _cameraRatio = MutableStateFlow("4:3")
    val cameraRatio = _cameraRatio.asStateFlow()

    private val _cameraMode = MutableStateFlow("Pose Match")
    val cameraMode = _cameraMode.asStateFlow()

    // Manual Pro settings overrides
    private val _proIso = MutableStateFlow(400)
    val proIso = _proIso.asStateFlow()

    private val _proExposure = MutableStateFlow(0.0f)
    val proExposure = _proExposure.asStateFlow()

    private val _proWb = MutableStateFlow("Auto")
    val proWb = _proWb.asStateFlow()

    // PRO live filter selection (see PhotoFilters). "auto" == look extracted from the overlay.
    private val _selectedFilterId = MutableStateFlow("auto")
    val selectedFilterId = _selectedFilterId.asStateFlow()

    // Runtime "Auto" look extracted from the current reference overlay image.
    private val _autoFilterMatrix = MutableStateFlow<FloatArray?>(null)
    val autoFilterMatrix = _autoFilterMatrix.asStateFlow()

    private val _autoFilterColor = MutableStateFlow<Int?>(null)
    val autoFilterColor = _autoFilterColor.asStateFlow()

    private val _proFocus = MutableStateFlow("Auto")
    val proFocus = _proFocus.asStateFlow()

    // On-device simulated drift variables
    private val _userDriftX = MutableStateFlow(0f)
    val userDriftX = _userDriftX.asStateFlow()

    private val _userDriftY = MutableStateFlow(0f)
    val userDriftY = _userDriftY.asStateFlow()

    // Real-time user landmarks from camera
    private val _userLandmarks = MutableStateFlow<Map<String, PointF>>(emptyMap())
    val userLandmarks = _userLandmarks.asStateFlow()

    // Landmarks detected from the current reference photo (empty until analysed).
    private val _referenceLandmarks = MutableStateFlow<Map<String, PointF>>(emptyMap())
    fun setReferenceLandmarks(map: Map<String, PointF>) { _referenceLandmarks.value = map }

    // Live AI matching telemetry calculations
    val poseMatchState: StateFlow<PoseMatchState> = combine(
        _selectedPose,
        _userLandmarks,
        _poseOffsetX,
        _poseOffsetY,
        combine(_poseScale, _poseRotation, _referenceLandmarks) { s, r, ref -> Triple(s, r, ref) }
    ) { pose, userPoints, ox, oy, triple ->
        val (scale, rot, refLm) = triple
        if (pose == null || userPoints.isEmpty()) {
            PoseMatchState()
        } else {
            PoseMatchAnalyzer.matchPose(
                userPoints = userPoints,
                referencePose = pose,
                referenceOverride = refLm,
                offsetX = ox,
                offsetY = oy,
                scale = scale,
                rotationDeg = rot
            )
        }
    }
        // Exponential smoothing on the score so it eases up/down instead of flickering.
        .scan(PoseMatchState()) { prev, curr ->
            val target = curr.similarityScore
            val smoothed = if (target == 0) 0
                else (0.35f * target + 0.65f * prev.similarityScore).roundToInt()
            curr.copy(similarityScore = smoothed)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(100), PoseMatchState())

    init {
        viewModelScope.launch {
            repository.getDefaultPoses().collect { list ->
                _defaultPoses.value = list
                if (list.isNotEmpty() && _selectedPose.value == null) {
                    _selectedPose.value = list.first()
                }
            }
        }
    }

    fun setOnboardingCompleted() {
        viewModelScope.launch { repository.setOnboardingCompleted(true) }
    }

    fun setTheme(theme: String) {
        viewModelScope.launch { repository.setAppTheme(theme) }
    }

    fun setRetainSkeleton(retain: Boolean) {
        viewModelScope.launch { repository.setRetainSkeleton(retain) }
    }

    fun setSearchQuery(query: String) { _searchQuery.value = query }

    fun setCategory(category: String) { _selectedCategory.value = category }

    fun selectPose(pose: PoseItem) {
        _selectedPose.value = pose
        _poseScale.value = 1.0f
        _poseOffsetX.value = 0f
        _poseOffsetY.value = 0f
        _poseRotation.value = 0f
    }

    fun selectPoseFromCustom(custom: CustomPose) {
        val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
        val mapType = Types.newParameterizedType(Map::class.java, String::class.java, Any::class.java)
        val adapter = moshi.adapter<Map<String, Any>>(mapType)
        val lmsMap = mutableMapOf<String, Float>()
        try {
            val rawMap = adapter.fromJson(custom.landmarksJson)
            rawMap?.forEach { (k, v) ->
                if (v is Double) lmsMap[k] = v.toFloat()
                else if (v is Float) lmsMap[k] = v
            }
        } catch (e: Exception) {}

        if (lmsMap.isEmpty()) {
            lmsMap.putAll(mapOf("head_x" to 0.5f, "head_y" to 0.25f, "left_shoulder_x" to 0.38f, "left_shoulder_y" to 0.45f, "right_shoulder_x" to 0.62f, "right_shoulder_y" to 0.45f))
        }

        val parsedPose = PoseItem(
            id = -(custom.id.toInt() + 1),
            title = custom.title,
            category = custom.category,
            description = custom.description,
            difficulty = custom.difficulty,
            tags = custom.tags.split(","),
            image = custom.imagePath,
            landmarks = lmsMap
        )
        _selectedPose.value = parsedPose
        _poseScale.value = 1.0f
        _poseOffsetX.value = 0f
        _poseOffsetY.value = 0f
        _poseRotation.value = 0f
    }

    fun updatePoseOpacity(opacity: Float) { _poseOpacity.value = opacity.coerceIn(0f, 1f) }

    fun updatePoseGestures(scale: Float, offsetX: Float, offsetY: Float, rotation: Float) {
        _poseScale.value = scale.coerceIn(0.2f, 4.0f)
        _poseOffsetX.value = offsetX.coerceIn(-0.8f, 0.8f)
        _poseOffsetY.value = offsetY.coerceIn(-0.8f, 0.8f)
        _poseRotation.value = rotation
    }

    fun updateDetectedPose(landmarks: Map<String, PointF>) {
        _userLandmarks.value = landmarks
    }

    fun toggleFlash() {
        _cameraFlashMode.value = when (_cameraFlashMode.value) {
            "Off" -> "On"
            "On" -> "Auto"
            else -> "Off"
        }
    }

    fun toggleCameraFacing() {
        _cameraFacing.value = if (_cameraFacing.value == "Back") "Front" else "Back"
    }

    fun setTimer(seconds: Int) { _cameraTimer.value = seconds }

    fun toggleGridVisible() { _cameraGridVisible.value = !_cameraGridVisible.value }

    fun setCameraMode(mode: String) { _cameraMode.value = mode }

    fun setProIso(iso: Int) { _proIso.value = iso }

    fun setProExposure(exp: Float) { _proExposure.value = exp }

    fun setFilter(id: String) { _selectedFilterId.value = id }

    fun setAutoFilter(matrix: FloatArray?, color: Int?) {
        _autoFilterMatrix.value = matrix
        _autoFilterColor.value = color
    }

    fun capturePhoto(localPath: String, title: String, place: PlaceInfo) {
        viewModelScope.launch {
            val score = poseMatchState.value.similarityScore
            val pose = selectedPose.value
            val record = CapturedPhoto(
                imagePath = localPath,
                dateTimestamp = System.currentTimeMillis(),
                poseId = pose?.id,
                matchScore = score,
                category = pose?.category ?: "Solo",
                title = title,
                locationName = place.name,
                latitude = place.latitude,
                longitude = place.longitude
            )
            repository.saveCapturedPhoto(record)
        }
    }

    fun deletePhoto(photo: CapturedPhoto) {
        viewModelScope.launch { repository.deleteCapturedPhoto(photo.id) }
    }

    fun toggleFavoritePhoto(photo: CapturedPhoto) {
        viewModelScope.launch { repository.toggleFavoritePhoto(photo.id, !photo.isFavorite) }
    }

    fun importPoseFromPath(title: String, imagePath: String) {
        viewModelScope.launch {
            val standardLms = mapOf("head_x" to 0.5f, "head_y" to 0.25f, "left_shoulder_x" to 0.38f, "left_shoulder_y" to 0.45f, "right_shoulder_x" to 0.62f, "right_shoulder_y" to 0.45f)
            val moshi = Moshi.Builder().build()
            val adapter = moshi.adapter(Map::class.java)
            val lmsJson = adapter.toJson(standardLms)

            val customPose = CustomPose(title = title, imagePath = imagePath, category = "User Imported", landmarksJson = lmsJson)
            repository.saveCustomPose(customPose)

            val immediatePose = PoseItem(id = -1, title = title, category = "User Imported", description = "Custom reference pose", difficulty = "Normal", tags = listOf("custom"), image = imagePath, landmarks = standardLms)
            _selectedPose.value = immediatePose
            _poseScale.value = 1.0f
            _poseOffsetX.value = 0f
            _poseOffsetY.value = 0f
            _poseRotation.value = 0f
        }
    }

    fun deleteCustomPose(pose: CustomPose) {
        viewModelScope.launch { repository.deleteCustomPose(pose.id) }
    }
}

class MainViewModelFactory(
    private val application: Application,
    private val repository: IAppRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(application, repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
