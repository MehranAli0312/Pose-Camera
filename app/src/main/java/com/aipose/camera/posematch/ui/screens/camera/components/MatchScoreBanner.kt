package com.aipose.camera.posematch.ui.screens.camera.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
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
import com.aipose.camera.posematch.ui.screens.camera.models.MatchFeedback

@Composable
internal fun MatchScoreBanner(
    score: Int,
    modifier: Modifier = Modifier,
) {
    val feedback = MatchFeedback.forScore(score)
    Column(
        modifier = modifier.padding(top = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(feedback.badgeColor.copy(alpha = 0.9f))
                .padding(horizontal = 20.dp, vertical = 6.dp)
        ) {
            Text(
                text = stringResource(R.string.match_badge, score),
                color = Color.White,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(14.dp))
                .background(Color.Black.copy(alpha = 0.55f))
                .padding(horizontal = 14.dp, vertical = 5.dp)
        ) {
            Text(
                text = stringResource(feedback.labelRes),
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
            )
        }
    }
}
