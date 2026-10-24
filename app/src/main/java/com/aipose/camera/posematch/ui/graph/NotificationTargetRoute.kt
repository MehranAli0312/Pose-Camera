package com.aipose.camera.posematch.ui.graph

import com.aipose.camera.posematch.domain.models.NotificationTarget

fun NotificationTarget.route(): String = when (this) {
    is NotificationTarget.CaptureDetail -> NavRoute.CaptureDetailScreenRoute.routeFor(captureId)
    is NotificationTarget.PoseDetail -> NavRoute.PoseDetailScreenRoute.routeFor(poseId)
    NotificationTarget.Camera -> NavRoute.CameraScreenRoute.routeWithoutPose()
}
