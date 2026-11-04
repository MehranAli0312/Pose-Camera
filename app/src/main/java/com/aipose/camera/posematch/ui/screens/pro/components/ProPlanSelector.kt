package com.aipose.camera.posematch.ui.screens.pro.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aipose.camera.posematch.R
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
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        when (state) {
            ProPlansState.Loading -> ProPlan.entries.forEach { plan ->
                ProPlanPlaceholderCard(plan = plan, isLoading = true)
            }

            ProPlansState.Unavailable -> ProPlan.entries.forEach { plan ->
                ProPlanPlaceholderCard(plan = plan, isLoading = false)
            }

            is ProPlansState.Content -> state.options.forEach { option ->
                val details = option.details
                ProPlanOptionCard(
                    plan = details.plan,
                    details = details,
                    selected = details == state.selectedPlan,
                    isLoading = false,
                    badge = proPlanBadge(
                        option = option,
                        isRecommended = details == state.recommendedPlan,
                    ),
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
        badge = null,
        enabled = false,
        onClick = {},
    )
}
