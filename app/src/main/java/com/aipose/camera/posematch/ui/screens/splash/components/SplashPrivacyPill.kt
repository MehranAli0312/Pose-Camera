package com.aipose.camera.posematch.ui.screens.splash.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aipose.camera.posematch.R
import com.aipose.camera.posematch.ui.theme.PoseTextMuted
import com.aipose.camera.posematch.ui.theme.poseTextStyle

private val PillHeight = 30.dp
private val PillShape = RoundedCornerShape(15.dp)
private val PillPadding = 16.dp
private val PillBorderWidth = 1.dp
private val SparkleSize = 12.dp
private val SparkleGap = 10.dp
private val NoteSize = 9.5.sp
private const val PILL_FILL_ALPHA = 0.04f
private const val PILL_BORDER_ALPHA = 0.07f

@Composable
internal fun SplashPrivacyPill(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .height(PillHeight)
            .clip(PillShape)
            .background(Color.White.copy(alpha = PILL_FILL_ALPHA))
            .border(PillBorderWidth, Color.White.copy(alpha = PILL_BORDER_ALPHA), PillShape)
            .padding(horizontal = PillPadding),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(SparkleGap),
    ) {
        Image(
            painter = painterResource(R.drawable.ic_pose_sparkle_diamond),
            contentDescription = null,
            modifier = Modifier.size(SparkleSize),
        )
        Text(
            text = stringResource(R.string.splash_privacy_note),
            style = poseTextStyle(NoteSize, FontWeight.Bold, PoseTextMuted),
        )
    }
}
