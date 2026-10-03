package com.aipose.camera.posematch.domain.models

data class SkeletonPoint(
    val joint: PoseJoint,
    val position: NormalizedPoint?
) {
    val isDetected: Boolean get() = position != null
}
