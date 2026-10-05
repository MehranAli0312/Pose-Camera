package com.aipose.camera.posematch.domain.repo

import com.aipose.camera.posematch.ads.ProRestoreResult
import com.aipose.camera.posematch.domain.models.PremiumPlan
import com.aipose.camera.posematch.domain.models.PremiumPurchaseOutcome
import kotlinx.coroutines.flow.Flow

interface PremiumRepository {

    val purchaseOutcomes: Flow<PremiumPurchaseOutcome>

    fun observeFreeUser(): Flow<Boolean>

    suspend fun loadPlans(): Result<List<PremiumPlan>>

    suspend fun restorePurchases(): ProRestoreResult
}
