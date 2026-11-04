package com.aipose.camera.posematch.ui.screens.pro.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aipose.camera.posematch.R
import com.aipose.camera.posematch.ui.theme.PosePremiumGold
import com.aipose.camera.posematch.ui.theme.PosePremiumGoldInk

private val ProPillShape = RoundedCornerShape(50)
private val PillLineWidth = 64.dp
private val PillLineGap = 12.dp
private const val PILL_LINE_ALPHA = 0.7f
const val BADGE_SHADOW_ALPHA = 0.45f

@Composable
internal fun ProHeader(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        ProPill()

        Spacer(Modifier.height(8.dp))

        Text(
            text = rememberProTitle(),
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp,
                lineHeight = 34.sp,
                color = MaterialTheme.colorScheme.onBackground,
            ),
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun ProPill() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(PillLineGap),
    ) {
        ProPillLine()
        Box(
            modifier = Modifier
                .clip(ProPillShape)
                .background(PosePremiumGold)
                .padding(horizontal = 18.dp, vertical = 2.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = stringResource(R.string.pose_pro_badge),
                style = MaterialTheme.typography.labelLarge.copy(
                    fontSize = 14.sp,
                    lineHeight = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = PosePremiumGoldInk,
                ),
                maxLines = 1,
            )
        }
        ProPillLine()
    }
}

@Composable
private fun ProPillLine() {
    Box(
        modifier = Modifier
            .width(PillLineWidth)
            .height(1.5.dp)
            .background(PosePremiumGold.copy(alpha = PILL_LINE_ALPHA)),
    )
}

@Composable
private fun rememberProTitle(): AnnotatedString {
    val title = stringResource(R.string.pro_title)
    val highlight = stringResource(R.string.pro_title_highlight)
    return remember(title, highlight) {
        buildAnnotatedString {
            append(title)
            val start = title.indexOf(highlight)
            if (start >= 0) {
                addStyle(
                    style = SpanStyle(color = PosePremiumGold),
                    start = start,
                    end = start + highlight.length,
                )
            }
        }
    }
}
