package com.aipose.camera.posematch.ui.screens.pro.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aipose.camera.posematch.R
import com.aipose.camera.posematch.ui.common.FloatingIcon3D
import com.aipose.camera.posematch.ui.models.Icon3DPalette
import com.aipose.camera.posematch.ui.theme.AppTheme

@Composable
internal fun ProHeader(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        ProCrownBadge()

        Spacer(Modifier.height(12.dp))

        Text(
            text = stringResource(R.string.pro_title),
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 26.sp,
                lineHeight = 32.sp,
                color = MaterialTheme.colorScheme.onBackground,
            ),
            textAlign = TextAlign.Center,
        )

        Spacer(Modifier.height(6.dp))

        Text(
            text = stringResource(R.string.pro_subtitle),
            style = MaterialTheme.typography.bodyMedium.copy(
                lineHeight = 20.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            ),
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
        )
    }
}

@Composable
private fun ProCrownBadge() {
    val glow = AppTheme.extendedColors.glowSecondary
    Box(
        modifier = Modifier
            .size(132.dp)
            .drawBehind {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(glow, Color.Transparent),
                        center = center,
                        radius = size.minDimension / 2f,
                    ),
                )
            },
        contentAlignment = Alignment.Center,
    ) {
        FloatingIcon3D(
            iconRes = R.drawable.ic_pro_glyph_crown,
            palette = Icon3DPalette.Brand,
            iconSize = 84.dp,
            boxSize = 120.dp,
            sparkleTint = MaterialTheme.colorScheme.primary,
        )
    }
}
