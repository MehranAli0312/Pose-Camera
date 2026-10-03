package com.aipose.camera.posematch.ui.screens.camera.models

import com.aipose.camera.posematch.domain.models.ColorGrade
import com.aipose.camera.posematch.domain.models.PhotoFilterId
import com.aipose.camera.posematch.domain.models.Pose
import com.aipose.camera.posematch.domain.models.PoseMatch

data class PoseCameraUiState(
    val poses: List<Pose> = emptyList(),
    val selectedPose: Pose? = null,
    val overlayCutoutPath: String? = null,
    val overlay: OverlayTransform = OverlayTransform(),
    val cameraFacing: CameraFacing = CameraFacing.Back,
    val flashMode: FlashMode = FlashMode.Off,
    val timer: CaptureTimer = CaptureTimer.Off,
    val countdownSeconds: Int = 0,
    val isGridVisible: Boolean = false,
    val areProControlsVisible: Boolean = false,
    val manualIso: Int = DEFAULT_ISO,
    val manualExposure: Float = 0f,
    val selectedFilter: PhotoFilterId = PhotoFilterId.Auto,
    val autoGrade: ColorGrade? = null,
    val autoSwatchColor: Int? = null,
    val activeGrade: ColorGrade? = null,
    val match: PoseMatch = PoseMatch(),
    val isSkeletonVisible: Boolean = true,
    val isCoachVisible: Boolean = false,
    val isGreatMatchVisible: Boolean = false,
    val isCapturing: Boolean = false,
    val pendingCaptureTarget: String? = null,
    val capturedPath: String? = null
) {
    val isCountdownRunning: Boolean get() = countdownSeconds > 0

    companion object {
        const val DEFAULT_ISO = 400
    }
}
