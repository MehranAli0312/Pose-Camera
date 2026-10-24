package com.aipose.camera.posematch.domain.usecase

import com.aipose.camera.posematch.domain.engagement.EngagementNotificationPicker
import com.aipose.camera.posematch.domain.models.Capture
import com.aipose.camera.posematch.domain.models.Pose
import com.aipose.camera.posematch.domain.models.PoseSource
import com.aipose.camera.posematch.domain.repo.CaptureRepository
import com.aipose.camera.posematch.domain.repo.EngagementNotificationRepository
import com.aipose.camera.posematch.domain.repo.PoseRepository
import com.aipose.camera.posematch.domain.repo.SplashStatusRepository
import com.aipose.camera.posematch.ui.firebaseRemote.AdsRemoteConfigStore
import kotlinx.coroutines.flow.first

class EngagementNotificationUseCase(
    private val notificationRepository: EngagementNotificationRepository,
    private val captureRepository: CaptureRepository,
    private val poseRepository: PoseRepository,
    private val splashStatusRepository: SplashStatusRepository,
    private val poseLockUseCase: PoseLockUseCase,
    private val captureProgressUseCase: CaptureProgressUseCase,
    private val remoteConfigStore: AdsRemoteConfigStore,
    private val picker: EngagementNotificationPicker,
) {

    suspend fun onAppForeground(nowMillis: Long = System.currentTimeMillis()) {
        notificationRepository.recordAppOpened(nowMillis)
        notificationRepository.scheduleDailyCheck(picker.preferredHour(captures()))
    }

    suspend fun onNotificationOpened() {
        notificationRepository.recordNotificationOpened()
    }

    suspend fun runDailyCheck(nowMillis: Long = System.currentTimeMillis()) {
        val captures = captures()
        try {
            if (isEnabled()) showBestNotification(captures, nowMillis)
        } finally {
            notificationRepository.scheduleFollowingCheck(picker.preferredHour(captures))
        }
    }

    private suspend fun showBestNotification(captures: List<Capture>, nowMillis: Long) {
        val notification = picker.pick(
            captures = captures,
            freePoses = freePoses(),
            history = notificationRepository.history(),
            currentStreak = captureProgressUseCase.currentStreak(captures, nowMillis),
            nowMillis = nowMillis,
        ) ?: return
        notificationRepository.show(notification, nowMillis)
    }

    private suspend fun isEnabled(): Boolean {
        remoteConfigStore.restore()
        return remoteConfigStore.current.localNotifications &&
            !splashStatusRepository.getSplashStatus().first()
    }

    private suspend fun captures(): List<Capture> = captureRepository.observeCaptures().first()

    private suspend fun freePoses(): List<Pose> {
        val poses = poseRepository.observePoses().first()
            .filter { pose -> pose.source == PoseSource.Bundled }
        val lockedIds = poseLockUseCase.observeAccess().first().lockedIdsIn(poses)
        return poses.filterNot { pose -> pose.id in lockedIds }
    }
}
