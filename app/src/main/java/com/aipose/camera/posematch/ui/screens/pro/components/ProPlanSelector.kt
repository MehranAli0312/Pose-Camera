package com.aipose.camera.posematch.ui.screens.pro.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.aipose.camera.posematch.domain.models.PremiumPlan
import com.aipose.camera.posematch.domain.models.ProPlan
import com.aipose.camera.posematch.ui.screens.pro.models.ProPlansState

@Composable
internal fun ProPlanSelector(
    state: ProPlansState,
    onSelectPlan: (PremiumPlan) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .selectableGroup(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        when (state) {
            ProPlansState.Loading -> ProPlan.entries.forEach { plan ->
                ProPlanPlaceholderCard(plan = plan, isLoading = true)
            }

            ProPlansState.Unavailable -> ProPlan.entries.forEach { plan ->
                ProPlanPlaceholderCard(plan = plan, isLoading = false)
            }

            is ProPlansState.Content -> state.plans.forEach { details ->
                ProPlanOptionCard(
                    plan = details.plan,
                    details = details,
                    selected = details == state.selectedPlan,
                    isLoading = false,
                    savePercent = state.yearlySavePercent.takeIf { details.plan == ProPlan.YEARLY },
                    showBestValue = details == state.recommendedPlan,
                    enabled = !state.isPurchasing,
                    onClick = { onSelectPlan(details) },
                )
            }
        }
    }
}

@Composable
private fun ProPlanPlaceholderCard(plan: ProPlan, isLoading: Boolean) {
    ProPlanOptionCard(
        plan = plan,
        details = null,
        selected = isLoading && plan == ProPlan.recommended,
        isLoading = isLoading,
        savePercent = null,
        showBestValue = isLoading && plan == ProPlan.recommended,
        enabled = false,
        onClick = {},
    )
}
