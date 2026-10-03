package com.aipose.camera.posematch.ui.screens.pro.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.aipose.camera.posematch.domain.models.ProPlan
import com.aipose.camera.posematch.ui.vm.ProUiState

@Composable
internal fun ProPlanSelector(
    state: ProUiState,
    onSelectPlan: (ProPlan) -> Unit,
    modifier: Modifier = Modifier,
) {
    val savePercent = state.yearlySavePercent
    Column(
        modifier = modifier
            .fillMaxWidth()
            .selectableGroup(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        state.planOptions.forEach { option ->
            val isYearly = option.plan == ProPlan.YEARLY
            val isSelectable = state.isLoadingProducts || option.isAvailable
            ProPlanOptionCard(
                option = option,
                selected = option.plan == state.selectedPlan,
                isLoading = state.isLoadingProducts,
                savePercent = savePercent.takeIf { isYearly },
                showBestValue = isYearly && isSelectable,
                enabled = isSelectable && !state.isPurchasing,
                onClick = { onSelectPlan(option.plan) },
            )
        }
    }
}
