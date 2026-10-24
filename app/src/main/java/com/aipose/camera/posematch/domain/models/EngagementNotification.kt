package com.aipose.camera.posematch.domain.models

sealed interface EngagementNotification {

    val target: NotificationTarget

    data class Memory(val capture: Capture) : EngagementNotification {
        override val target: NotificationTarget = NotificationTarget.CaptureDetail(capture.id)
    }

    data class StreakSaver(val streakDays: Int) : EngagementNotification {
        override val target: NotificationTarget = NotificationTarget.Camera
    }

    data class FirstPhoto(val pose: Pose?) : EngagementNotification {
        override val target: NotificationTarget =
            pose?.let { NotificationTarget.PoseDetail(it.id) } ?: NotificationTarget.Camera
    }

    data class PoseIdea(val pose: Pose) : EngagementNotification {
        override val target: NotificationTarget = NotificationTarget.PoseDetail(pose.id)
    }
}
