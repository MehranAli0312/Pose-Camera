package com.aipose.camera.posematch.data.notification

import android.content.Intent
import com.aipose.camera.posematch.domain.models.NotificationTarget

private const val EXTRA_TARGET_KIND = "notification_target_kind"
private const val EXTRA_TARGET_ID = "notification_target_id"
private const val KIND_CAPTURE_DETAIL = "capture_detail"
private const val KIND_POSE_DETAIL = "pose_detail"
private const val KIND_CAMERA = "camera"
private const val NO_ID = Long.MIN_VALUE

fun Intent.putNotificationTarget(target: NotificationTarget): Intent = when (target) {
    is NotificationTarget.CaptureDetail -> putExtra(EXTRA_TARGET_KIND, KIND_CAPTURE_DETAIL)
        .putExtra(EXTRA_TARGET_ID, target.captureId)

    is NotificationTarget.PoseDetail -> putExtra(EXTRA_TARGET_KIND, KIND_POSE_DETAIL)
        .putExtra(EXTRA_TARGET_ID, target.poseId.toLong())

    NotificationTarget.Camera -> putExtra(EXTRA_TARGET_KIND, KIND_CAMERA)
}

fun Intent.notificationTarget(): NotificationTarget? {
    val id = getLongExtra(EXTRA_TARGET_ID, NO_ID)
    return when (getStringExtra(EXTRA_TARGET_KIND)) {
        KIND_CAPTURE_DETAIL -> id.takeIf { it != NO_ID }?.let(NotificationTarget::CaptureDetail)
        KIND_POSE_DETAIL -> id.takeIf { it != NO_ID }?.let { NotificationTarget.PoseDetail(it.toInt()) }
        KIND_CAMERA -> NotificationTarget.Camera
        else -> null
    }
}

fun Intent.clearNotificationTarget() {
    removeExtra(EXTRA_TARGET_KIND)
    removeExtra(EXTRA_TARGET_ID)
}
