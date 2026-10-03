package com.aipose.camera.posematch.ui.screens.onboard.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.aipose.camera.posematch.R
import com.aipose.camera.posematch.ui.common.bounceClick
import com.aipose.camera.posematch.ui.theme.AppTheme

@Composable
fun OnboardSkipPill(onSkip: () -> Unit) {
    val colors = AppTheme.extendedColors
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 16.dp),
    ) {
        Text(
            text = stringResource(R.string.skip),
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier
                .bounceClick(onClick = onSkip)
                .shadow(
                    8.dp,
                    CircleShape,
                    ambientColor = colors.contactShadow,
                    spotColor = colors.contactShadow
                )
                .clip(CircleShape)
                .background(colors.glassSurface)
                .border(1.dp, colors.glassBorder, CircleShape)
                .padding(horizontal = 18.dp, vertical = 8.dp)
                .align(Alignment.CenterEnd),
        )
    }
}
