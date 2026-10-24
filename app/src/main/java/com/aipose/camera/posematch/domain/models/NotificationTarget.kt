package com.aipose.camera.posematch.domain.models

sealed interface NotificationTarget {
    data class CaptureDetail(val captureId: Long) : NotificationTarget
    data class PoseDetail(val poseId: Int) : NotificationTarget
    data object Camera : NotificationTarget
}
