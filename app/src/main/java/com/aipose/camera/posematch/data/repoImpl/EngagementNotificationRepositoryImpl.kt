package com.aipose.camera.posematch.data.repoImpl

import com.aipose.camera.posematch.data.local.AppDataStore
import com.aipose.camera.posematch.data.notification.EngagementNotificationDataSource
import com.aipose.camera.posematch.data.notification.EngagementWorkScheduler
import com.aipose.camera.posematch.domain.models.EngagementHistory
import com.aipose.camera.posematch.domain.models.EngagementNotification
import com.aipose.camera.posematch.domain.repo.EngagementNotificationRepository
import kotlinx.coroutines.flow.first

class EngagementNotificationRepositoryImpl(
    private val appDataStore: AppDataStore,
    private val notificationDataSource: EngagementNotificationDataSource,
    private val workScheduler: EngagementWorkScheduler,
) : EngagementNotificationRepository {

    override suspend fun history(): EngagementHistory = appDataStore.getEngagementHistory()

    override suspend fun recordAppOpened(nowMillis: Long) {
        appDataStore.setLastAppOpenAt(nowMillis)
    }

    override suspend fun recordNotificationOpened() {
        appDataStore.resetEngagementUnopened()
    }

    override suspend fun show(notification: EngagementNotification, nowMillis: Long) {
        val languageCode = appDataStore.getLanguageCode().first()
        if (notificationDataSource.post(notification, languageCode)) {
            appDataStore.recordEngagementSent(nowMillis)
        }
    }

    override fun scheduleDailyCheck(hourOfDay: Int) {
        workScheduler.replace(hourOfDay)
    }

    override fun scheduleFollowingCheck(hourOfDay: Int) {
        workScheduler.appendAfterCurrent(hourOfDay)
    }
}
