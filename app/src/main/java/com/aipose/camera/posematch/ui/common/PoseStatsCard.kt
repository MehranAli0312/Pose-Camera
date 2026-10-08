package com.aipose.camera.posematch.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aipose.camera.posematch.ui.models.PoseStat
import com.aipose.camera.posematch.ui.theme.LocalAppPalette
import com.aipose.camera.posematch.ui.theme.poseTextStyle

private val CardShape = RoundedCornerShape(22.dp)
private val DividerHeight = 36.dp
private const val CARD_BORDER_ALPHA = 0.1f
private const val DIVIDER_ALPHA = 0.08f
private const val TITLED_GLOSS_ALPHA = 0.16f

@Composable
fun PoseStatsCard(
    stats: List<PoseStat>,
    modifier: Modifier = Modifier,
    title: String? = null,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .poseRaisedSurface(
                shape = CardShape,
                borderAlpha = CARD_BORDER_ALPHA,
                glossAlpha = if (title != null) TITLED_GLOSS_ALPHA else 0f,
            )
            .padding(vertical = 12.dp)
    ) {
        if (title != null) {
            Text(
                text = title,
                style = poseTextStyle(13.5.sp, FontWeight.Bold, Color.White),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(horizontal = 16.dp),
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            stats.forEachIndexed { index, stat ->
                if (index > 0) StatDivider()
                StatColumn(stat = stat, modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun StatColumn(
    stat: PoseStat,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(5.dp),
    ) {
        Text(
            text = stat.value,
            style = poseTextStyle(18.sp, FontWeight.Bold, stat.valueColor),
            maxLines = 1,
        )
        Text(
            text = stat.label,
            style = poseTextStyle(8.5.sp, FontWeight.Bold, LocalAppPalette.current.textFaint),
            maxLines = 1,
        )
    }
}

@Composable
private fun StatDivider() {
    Box(
        modifier = Modifier
            .width(1.dp)
            .height(DividerHeight)
            .background(Color.White.copy(alpha = DIVIDER_ALPHA))
    )
}
