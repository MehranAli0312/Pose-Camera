package com.aipose.camera.posematch.data.notification

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.aipose.camera.posematch.domain.usecase.EngagementNotificationUseCase
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class EngagementNotificationWorker(
    context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params), KoinComponent {

    private val engagementNotificationUseCase: EngagementNotificationUseCase by inject()

    override suspend fun doWork(): Result {
        runCatching { engagementNotificationUseCase.runDailyCheck() }
        return Result.success()
    }
}
