package com.aipose.camera.posematch.domain.repo

import com.aipose.camera.posematch.domain.models.EngagementHistory
import com.aipose.camera.posematch.domain.models.EngagementNotification

interface EngagementNotificationRepository {

    suspend fun history(): EngagementHistory

    suspend fun recordAppOpened(nowMillis: Long)

    suspend fun recordNotificationOpened()

    suspend fun show(notification: EngagementNotification, nowMillis: Long)

    fun scheduleDailyCheck(hourOfDay: Int)

    fun scheduleFollowingCheck(hourOfDay: Int)
}
