package com.aipose.camera.posematch.data.notification

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import java.util.Calendar
import java.util.concurrent.TimeUnit

class EngagementWorkScheduler(private val context: Context) {

    fun replace(hourOfDay: Int) {
        enqueue(hourOfDay, ExistingWorkPolicy.REPLACE)
    }

    fun appendAfterCurrent(hourOfDay: Int) {
        enqueue(hourOfDay, ExistingWorkPolicy.APPEND_OR_REPLACE)
    }

    private fun enqueue(hourOfDay: Int, policy: ExistingWorkPolicy) {
        val request = OneTimeWorkRequestBuilder<EngagementNotificationWorker>()
            .setInitialDelay(delayUntilNext(hourOfDay), TimeUnit.MILLISECONDS)
            .build()
        runCatching {
            WorkManager.getInstance(context).enqueueUniqueWork(WORK_NAME, policy, request)
        }
    }

    private fun delayUntilNext(hourOfDay: Int): Long {
        val now = System.currentTimeMillis()
        val next = Calendar.getInstance().apply {
            timeInMillis = now
            set(Calendar.HOUR_OF_DAY, hourOfDay)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (timeInMillis - now < MIN_DELAY_MS) add(Calendar.DAY_OF_YEAR, 1)
        }
        return next.timeInMillis - now
    }

    private companion object {
        const val WORK_NAME = "engagement_notification_check"
        const val MIN_DELAY_MS = 60_000L
    }
}
