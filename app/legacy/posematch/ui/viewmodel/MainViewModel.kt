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
import kotlinx.coroutines.flow.*
import kotlinx.serialization.json.Json
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

    val selectedLanguage: StateFlow<String> = repository.selectedLanguage
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "English")

    val appTheme: StateFlow<String> = repository.appTheme
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "Dark")

    val retainSkeleton: StateFlow<Boolean> = repository.retainSkeleton
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    // Default & Custom Poses
    private val _defaultPoses = MutableStateFlow<List<PoseItem>>(emptyList())
    val defaultPoses: StateFlow<List<PoseItem>> = _defaultPoses.asStateFlow()

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

    // Manual Pro settings overrides
    private val _proIso = MutableStateFlow(400)
    val proIso = _proIso.asStateFlow()

    private val _proExposure = MutableStateFlow(0.0f)
    val proExposure = _proExposure.asStateFlow()

    // Runtime "Auto" look extracted from the current reference overlay image.
    private val _autoFilterMatrix = MutableStateFlow<FloatArray?>(null)
    val autoFilterMatrix = _autoFilterMatrix.asStateFlow()

    private val _autoFilterColor = MutableStateFlow<Int?>(null)
    val autoFilterColor = _autoFilterColor.asStateFlow()

    // Real-time user landmarks from camera
    private val _userLandmarks = MutableStateFlow<Map<String, PointF>>(emptyMap())

    // Landmarks detected from the current reference photo (empty until analysed).
    private val _referenceLandmarks = MutableStateFlow<Map<String, PointF>>(emptyMap())
    fun setReferenceLandmarks(map: Map<String, PointF>) { _referenceLandmarks.value = map }

    // Live AI matching telemetry calculations
    val poseMatchState: StateFlow<PoseMatchState> = combine(
        _selectedPose,
        _userLandmarks,
        _referenceLandmarks
    ) { pose, userPoints, refLm ->
        if (pose == null || userPoints.isEmpty()) {
            PoseMatchState()
        } else {
            PoseMatchAnalyzer.matchPose(
                userPoints = userPoints,
                referencePose = pose,
                referenceOverride = refLm
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

    fun setLanguage(language: String) {
        viewModelScope.launch { repository.setSelectedLanguage(language) }
    }

    fun setTheme(theme: String) {
        viewModelScope.launch { repository.setAppTheme(theme) }
    }

    fun setRetainSkeleton(retain: Boolean) {
        viewModelScope.launch { repository.setRetainSkeleton(retain) }
    }

    fun setSearchQuery(query: String) { _searchQuery.value = query }

    fun selectPose(pose: PoseItem) {
        _selectedPose.value = pose
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

    fun setProIso(iso: Int) { _proIso.value = iso }

    fun setProExposure(exp: Float) { _proExposure.value = exp }

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
            val lmsJson = Json.encodeToString(standardLms)

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
