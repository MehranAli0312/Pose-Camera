package com.aipose.camera.posematch.ui.vm

import androidx.camera.core.ImageAnalysis
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aipose.camera.posematch.data.pose.PoseFrameAnalyzer
import com.aipose.camera.posematch.domain.models.CaptureDraft
import com.aipose.camera.posematch.domain.models.NormalizedPoint
import com.aipose.camera.posematch.domain.models.PhotoFilterId
import com.aipose.camera.posematch.domain.models.Pose
import com.aipose.camera.posematch.domain.models.PoseJoint
import com.aipose.camera.posematch.domain.usecase.CameraSettingsUseCase
import com.aipose.camera.posematch.domain.usecase.CaptureUseCase
import com.aipose.camera.posematch.domain.usecase.PhotoEditUseCase
import com.aipose.camera.posematch.domain.usecase.PoseLibraryUseCase
import com.aipose.camera.posematch.domain.usecase.PoseMatchUseCase
import com.aipose.camera.posematch.ui.screens.camera.models.CameraTool
import com.aipose.camera.posematch.ui.models.CaptureTimer
import com.aipose.camera.posematch.ui.screens.camera.models.PoseCameraUiState
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class PoseCameraViewModel(
    private val poseLibraryUseCase: PoseLibraryUseCase,
    private val poseMatchUseCase: PoseMatchUseCase,
    private val captureUseCase: CaptureUseCase,
    private val cameraSettingsUseCase: CameraSettingsUseCase,
    private val photoEditUseCase: PhotoEditUseCase,
    private val poseFrameAnalyzer: PoseFrameAnalyzer
) : ViewModel() {

    private val _uiState = MutableStateFlow(PoseCameraUiState())
    val uiState = _uiState.asStateFlow()

    private var referenceLandmarks: Map<PoseJoint, NormalizedPoint> = emptyMap()
    private var referenceJob: Job? = null
    private var countdownJob: Job? = null
    private var greatMatchJob: Job? = null
    private var bestScoreJob: Job? = null
    private var requestedPoseId: Int? = null

    val frameAnalyzer: ImageAnalysis.Analyzer = poseFrameAnalyzer

    init {
        poseFrameAnalyzer.onLandmarks = ::onUserLandmarks
        observePoses()
        observeSkeletonPreference()
        observeTimerPreference()
        observeCoachVisibility()
        observeGalleryCount()
    }

    fun onPoseRequested(poseId: Int) {
        if (requestedPoseId == poseId) return
        requestedPoseId = poseId
        _uiState.value.poses.firstOrNull { it.id == poseId }?.let(::selectPose)
    }

    fun selectPose(pose: Pose) {
        if (_uiState.value.selectedPose?.id == pose.id) {
            hidePosePicker()
            return
        }
        _uiState.update { state ->
            state.copy(
                selectedPose = pose,
                isPosePickerVisible = false,
                bestScore = 0,
                overlay = state.overlay.withGestures(
                    scale = OVERLAY_RESET_SCALE,
                    offsetX = 0f,
                    offsetY = 0f,
                    rotation = 0f
                ),
                overlayCutoutPath = null,
                autoGrade = null,
                autoSwatchColor = null,
                activeGrade = null
            )
        }
        loadReference(pose)
        observeBestScore(pose.id)
    }

    fun showPosePicker() {
        _uiState.update { state -> state.copy(isPosePickerVisible = true) }
    }

    fun hidePosePicker() {
        _uiState.update { state -> state.copy(isPosePickerVisible = false) }
    }

    fun importPose(title: String, sourceUri: String) {
        if (_uiState.value.isImporting) return
        _uiState.update { state -> state.copy(isImporting = true) }
        viewModelScope.launch {
            val imported = poseLibraryUseCase.importPose(title, sourceUri)
            _uiState.update { state ->
                state.copy(isImporting = false, isImportFailed = imported == null)
            }
            if (imported != null) {
                requestedPoseId = imported.id
                selectPose(imported)
            }
        }
    }

    fun onImportFailureHandled() {
        _uiState.update { state -> state.copy(isImportFailed = false) }
    }

    fun onToolClicked(tool: CameraTool) {
        when (tool) {
            CameraTool.Grid -> toggleGrid()
            CameraTool.Timer -> showTimerSheet()
            CameraTool.Skeleton -> setSkeletonVisible(!_uiState.value.isSkeletonVisible)
            CameraTool.Pro -> toggleProControls()
        }
    }

    fun updateOverlayOpacity(opacity: Float) {
        _uiState.update { state -> state.copy(overlay = state.overlay.withOpacity(opacity)) }
    }

    fun updateOverlayGestures(scale: Float, offsetX: Float, offsetY: Float, rotation: Float) {
        _uiState.update { state ->
            state.copy(overlay = state.overlay.withGestures(scale, offsetX, offsetY, rotation))
        }
    }

    fun toggleFlashMode() {
        _uiState.update { state -> state.copy(flashMode = state.flashMode.next()) }
    }

    fun toggleCameraFacing() {
        _uiState.update { state ->
            val facing = state.cameraFacing.toggled()
            poseFrameAnalyzer.isFrontCamera = facing.isFront
            state.copy(cameraFacing = facing)
        }
    }

    fun toggleProControls() {
        _uiState.update { state -> state.copy(areProControlsVisible = !state.areProControlsVisible) }
    }

    fun showTimerSheet() {
        _uiState.update { state -> state.copy(isTimerSheetVisible = true) }
    }

    fun hideTimerSheet() {
        _uiState.update { state -> state.copy(isTimerSheetVisible = false) }
    }

    fun setTimer(timer: CaptureTimer) {
        _uiState.update { state -> state.copy(timer = timer, isTimerSheetVisible = false) }
        viewModelScope.launch { cameraSettingsUseCase.setCaptureTimerSeconds(timer.seconds) }
    }

    fun cycleTimer() {
        setTimer(_uiState.value.timer.next())
    }

    fun toggleGrid() {
        _uiState.update { state -> state.copy(isGridVisible = !state.isGridVisible) }
    }

    fun setManualIso(iso: Int) {
        _uiState.update { state -> state.copy(manualIso = iso) }
    }

    fun setManualExposure(exposure: Float) {
        _uiState.update { state -> state.copy(manualExposure = exposure) }
    }

    fun selectFilter(filterId: PhotoFilterId) {
        _uiState.update { state ->
            state.copy(
                selectedFilter = filterId,
                activeGrade = photoEditUseCase.gradeFor(filterId, state.autoGrade)
            )
        }
    }

    fun setSkeletonVisible(visible: Boolean) {
        viewModelScope.launch { cameraSettingsUseCase.setRetainSkeleton(visible) }
    }

    fun dismissCoach() {
        _uiState.update { state -> state.copy(isCoachVisible = false) }
        viewModelScope.launch { cameraSettingsUseCase.markCameraCoachSeen() }
    }

    fun onShutterClicked() {
        val state = _uiState.value
        if (state.isCapturing || state.isCountdownRunning) return
        val timerSeconds = state.timer.seconds
        if (timerSeconds <= 0) {
            prepareCaptureTarget()
            return
        }
        countdownJob?.cancel()
        countdownJob = viewModelScope.launch {
            var remaining = timerSeconds
            while (remaining > 0) {
                _uiState.update { current -> current.copy(countdownSeconds = remaining) }
                delay(COUNTDOWN_STEP_MILLIS)
                remaining--
            }
            _uiState.update { current -> current.copy(countdownSeconds = 0) }
            prepareCaptureTarget()
        }
    }

    fun onCaptureSucceeded(path: String) {
        val state = _uiState.value
        captureUseCase.putDraft(
            CaptureDraft(
                imagePath = path,
                poseId = state.selectedPose?.id,
                poseTitle = state.selectedPose?.title.orEmpty(),
                category = state.selectedPose?.category.orEmpty(),
                matchScore = state.match.score,
                referenceImagePath = state.selectedPose?.imagePath
            )
        )
        _uiState.update { current ->
            current.copy(isCapturing = false, pendingCaptureTarget = null, capturedPath = path)
        }
    }

    fun onCaptureFailed() {
        val target = _uiState.value.pendingCaptureTarget
        _uiState.update { state -> state.copy(isCapturing = false, pendingCaptureTarget = null) }
        if (target != null) viewModelScope.launch { captureUseCase.discardFile(target) }
    }

    fun onCapturedPathHandled() {
        _uiState.update { state -> state.copy(capturedPath = null) }
    }

    private fun prepareCaptureTarget() {
        viewModelScope.launch {
            val target = captureUseCase.createCaptureTarget()
            _uiState.update { state ->
                state.copy(isCapturing = true, pendingCaptureTarget = target)
            }
        }
    }

    private fun onUserLandmarks(landmarks: Map<PoseJoint, NormalizedPoint>) {
        _uiState.update { state ->
            val match = poseMatchUseCase.match(state.selectedPose, landmarks, referenceLandmarks)
            val smoothedScore = poseMatchUseCase.smoothScore(state.match.score, match.score)
            state.copy(match = match.copy(score = smoothedScore))
        }
        maybeCelebrate(_uiState.value.match.score)
    }

    private fun maybeCelebrate(score: Int) {
        val isGreat = score >= GREAT_MATCH_SCORE
        if (!isGreat || _uiState.value.isGreatMatchVisible) return
        greatMatchJob?.cancel()
        greatMatchJob = viewModelScope.launch {
            _uiState.update { state -> state.copy(isGreatMatchVisible = true) }
            delay(GREAT_MATCH_VISIBLE_MILLIS)
            _uiState.update { state -> state.copy(isGreatMatchVisible = false) }
        }
    }

    private fun observePoses() {
        viewModelScope.launch {
            poseLibraryUseCase.observePoses().collect { poses ->
                _uiState.update { state -> state.copy(poses = poses) }
                val requested = requestedPoseId
                val current = _uiState.value.selectedPose
                when {
                    requested != null && current?.id != requested ->
                        poses.firstOrNull { it.id == requested }?.let(::selectPose)

                    current == null -> poses.firstOrNull()?.let(::selectPose)
                }
            }
        }
    }

    private fun observeTimerPreference() {
        viewModelScope.launch {
            cameraSettingsUseCase.getCaptureTimerSeconds().collect { seconds ->
                _uiState.update { state -> state.copy(timer = CaptureTimer.fromSeconds(seconds)) }
            }
        }
    }

    private fun observeSkeletonPreference() {
        viewModelScope.launch {
            cameraSettingsUseCase.getRetainSkeleton().collect { retain ->
                _uiState.update { state -> state.copy(isSkeletonVisible = retain) }
            }
        }
    }

    private fun observeGalleryCount() {
        viewModelScope.launch {
            captureUseCase.observeCaptureCount().collect { count ->
                _uiState.update { state -> state.copy(galleryCount = count) }
            }
        }
    }

    private fun observeBestScore(poseId: Int) {
        bestScoreJob?.cancel()
        bestScoreJob = viewModelScope.launch {
            captureUseCase.observeBestScore(poseId).collect { best ->
                _uiState.update { state ->
                    if (state.selectedPose?.id == poseId) state.copy(bestScore = best) else state
                }
            }
        }
    }

    private fun observeCoachVisibility() {
        viewModelScope.launch {
            val isSeen = cameraSettingsUseCase.isCameraCoachSeen()
            isSeen.collect { seen ->
                _uiState.update { state -> state.copy(isCoachVisible = !seen) }
            }
        }
    }

    private fun loadReference(pose: Pose) {
        referenceJob?.cancel()
        referenceLandmarks = emptyMap()
        referenceJob = viewModelScope.launch {
            val cutoutPath = poseLibraryUseCase.cutoutPath(pose)
            val look = photoEditUseCase.extractReferenceLook(pose.imagePath)
            referenceLandmarks = poseLibraryUseCase.detectLandmarks(pose.imagePath)
            _uiState.update { state ->
                if (state.selectedPose?.id != pose.id) {
                    state
                } else {
                    state.copy(
                        overlayCutoutPath = cutoutPath,
                        autoGrade = look?.grade,
                        autoSwatchColor = look?.dominantColor,
                        activeGrade = photoEditUseCase.gradeFor(state.selectedFilter, look?.grade)
                    )
                }
            }
        }
    }

    override fun onCleared() {
        poseFrameAnalyzer.onLandmarks = null
        super.onCleared()
    }

    private companion object {
        const val GREAT_MATCH_SCORE = 85
        const val GREAT_MATCH_VISIBLE_MILLIS = 1_300L
        const val COUNTDOWN_STEP_MILLIS = 1_000L
        const val OVERLAY_RESET_SCALE = 1f
    }
}
