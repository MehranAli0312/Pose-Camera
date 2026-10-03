package com.aipose.camera.posematch.ui.screens.pro.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.aipose.camera.posematch.R
import com.aipose.camera.posematch.ui.common.AccessBenefitsCard
import com.aipose.camera.posematch.ui.screens.pro.models.proIncludedFeatures
import com.aipose.camera.posematch.ui.theme.AppTheme

@Composable
internal fun ProIncludedFeatures(modifier: Modifier = Modifier) {
    AccessBenefitsCard(
        benefits = proIncludedFeatures,
        modifier = modifier,
        trailingContent = { ProIncludedCheck() },
    )
}

@Composable
private fun ProIncludedCheck() {
    val colors = AppTheme.extendedColors
    Box(
        modifier = Modifier
            .size(22.dp)
            .clip(CircleShape)
            .background(colors.softAccentGreen),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_check_mark),
            contentDescription = null,
            tint = colors.onSoftAccentGreen,
            modifier = Modifier.size(14.dp),
        )
    }
}
