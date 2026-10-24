package com.aipose.camera.posematch.data.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.aipose.camera.posematch.data.local.AppDataStore
import com.aipose.camera.posematch.domain.models.EngagementNotification
import com.aipose.camera.posematch.domain.models.PoseSource
import com.aipose.camera.posematch.domain.repo.CaptureRepository
import com.aipose.camera.posematch.domain.repo.PoseRepository
import com.aipose.camera.posematch.domain.usecase.EngagementNotificationUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class DebugNotificationReceiver : BroadcastReceiver(), KoinComponent {

    private val notificationDataSource: EngagementNotificationDataSource by inject()
    private val engagementNotificationUseCase: EngagementNotificationUseCase by inject()
    private val captureRepository: CaptureRepository by inject()
    private val poseRepository: PoseRepository by inject()
    private val appDataStore: AppDataStore by inject()

    override fun onReceive(context: Context, intent: Intent) {
        val pending = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                trigger(intent.getStringExtra(EXTRA_TYPE).orEmpty())
            } finally {
                pending.finish()
            }
        }
    }

    private suspend fun trigger(type: String) {
        if (type == TYPE_DAILY_CHECK) {
            engagementNotificationUseCase.runDailyCheck()
            return
        }
        val notification = sampleOf(type) ?: return
        notificationDataSource.post(notification, appDataStore.getLanguageCode().first())
    }

    private suspend fun sampleOf(type: String): EngagementNotification? {
        val pose = poseRepository.observePoses().first()
            .filter { it.source == PoseSource.Bundled }
            .randomOrNull()
        return when (type) {
            TYPE_MEMORY -> captureRepository.observeCaptures().first()
                .firstOrNull()
                ?.let(EngagementNotification::Memory)
            TYPE_STREAK -> EngagementNotification.StreakSaver(SAMPLE_STREAK_DAYS)
            TYPE_FIRST_PHOTO -> EngagementNotification.FirstPhoto(pose)
            TYPE_POSE_IDEA -> pose?.let(EngagementNotification::PoseIdea)
            else -> null
        }
    }

    private companion object {
        const val EXTRA_TYPE = "type"
        const val TYPE_DAILY_CHECK = "check"
        const val TYPE_MEMORY = "memory"
        const val TYPE_STREAK = "streak"
        const val TYPE_FIRST_PHOTO = "first"
        const val TYPE_POSE_IDEA = "pose"
        const val SAMPLE_STREAK_DAYS = 5
    }
}
