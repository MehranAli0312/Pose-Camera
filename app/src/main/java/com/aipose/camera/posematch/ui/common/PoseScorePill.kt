package com.aipose.camera.posematch.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aipose.camera.posematch.R
import com.aipose.camera.posematch.domain.models.PERFECT_MATCH_SCORE
import com.aipose.camera.posematch.ui.theme.PoseScoreHigh
import com.aipose.camera.posematch.ui.theme.PoseScoreLow
import com.aipose.camera.posematch.ui.theme.poseTextStyle
import com.aipose.camera.posematch.util.bidiIsolate

private val PillShape = RoundedCornerShape(11.dp)
private val DotSize = 6.4.dp
private const val PILL_ALPHA = 0.52f

@Composable
fun PoseScorePill(score: Int, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .clip(PillShape)
            .background(Color.Black.copy(alpha = PILL_ALPHA))
            .padding(horizontal = 11.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp),
    ) {
        Box(
            modifier = Modifier
                .size(DotSize)
                .clip(CircleShape)
                .background(if (score >= PERFECT_MATCH_SCORE) PoseScoreHigh else PoseScoreLow),
        )
        Text(
            text = stringResource(R.string.score_percent, score).bidiIsolate(),
            style = poseTextStyle(9.5.sp, FontWeight.Bold, Color.White),
            maxLines = 1,
        )
    }
}
