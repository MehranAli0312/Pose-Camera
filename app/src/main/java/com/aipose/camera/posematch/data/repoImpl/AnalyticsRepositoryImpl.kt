package com.aipose.camera.posematch.data.repoImpl

import com.aipose.camera.posematch.data.analytics.AnalyticsDataSource
import com.aipose.camera.posematch.data.local.AppDataStore
import com.aipose.camera.posematch.domain.models.AnalyticsEvent
import com.aipose.camera.posematch.domain.models.UserFlow
import com.aipose.camera.posematch.domain.repo.AnalyticsRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class AnalyticsRepositoryImpl(
    private val dataStore: AppDataStore,
    private val analyticsDataSource: AnalyticsDataSource,
) : AnalyticsRepository {

    private val userFlowMutex = Mutex()

    @Volatile
    private var launchUserFlow: UserFlow? = null

    override suspend fun userFlow(): UserFlow =
        launchUserFlow ?: userFlowMutex.withLock {
            launchUserFlow ?: resolveLaunchUserFlow().also { flow ->
                analyticsDataSource.setUserProperty(USER_FLOW_PROPERTY, flow.value)
                launchUserFlow = flow
            }
        }

    override suspend fun markFirstOccurrence(event: AnalyticsEvent): Boolean =
        dataStore.markAnalyticsEventFirstOccurrence(event.eventName)

    override fun logEvent(eventName: String, params: Map<String, String>) {
        analyticsDataSource.logEvent(eventName, params)
    }

    private suspend fun resolveLaunchUserFlow(): UserFlow {
        val isOnboardingPending = dataStore.readOnBoardStatus().first()
        return if (isOnboardingPending) UserFlow.FirstTime else UserFlow.Returning
    }

    private companion object {
        const val USER_FLOW_PROPERTY = "user_flow"
    }
}
