package com.aipose.camera.posematch.ui.screens.photoSuccess.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aipose.camera.posematch.R
import com.aipose.camera.posematch.ui.common.GlossyIconCircle
import com.aipose.camera.posematch.ui.models.GlossyBadgePalette
import com.aipose.camera.posematch.ui.theme.Emerald
import com.aipose.camera.posematch.ui.theme.LocalAppPalette
import com.aipose.camera.posematch.ui.theme.poseTextStyle

private val OuterRingSize = 128.dp
private val InnerRingSize = 100.dp
private val BadgeSize = 80.dp
private val CheckGlyphSize = 34.dp
private const val OUTER_RING_ALPHA = 0.1f
private const val OUTER_RING_BORDER_ALPHA = 0.22f
private const val INNER_RING_ALPHA = 0.14f

@Composable
internal fun PhotoSuccessHeader(
    subtitle: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        SavedCheckBadge()
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = stringResource(R.string.saved_title),
            style = poseTextStyle(25.sp, FontWeight.Bold, Color.White),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 20.dp),
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = subtitle,
            style = poseTextStyle(12.5.sp, FontWeight.Normal, LocalAppPalette.current.textMuted),
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(horizontal = 20.dp),
        )
    }
}

@Composable
private fun SavedCheckBadge(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(OuterRingSize)
            .clip(CircleShape)
            .background(Emerald.copy(alpha = OUTER_RING_ALPHA))
            .border(1.dp, Emerald.copy(alpha = OUTER_RING_BORDER_ALPHA), CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .size(InnerRingSize)
                .clip(CircleShape)
                .background(Emerald.copy(alpha = INNER_RING_ALPHA)),
            contentAlignment = Alignment.Center,
        ) {
            GlossyIconCircle(
                iconRes = R.drawable.ic_check_mark,
                palette = GlossyBadgePalette.Emerald,
                size = BadgeSize,
                glyphSize = CheckGlyphSize,
            )
        }
    }
}
