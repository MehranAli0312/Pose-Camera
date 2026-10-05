package com.aipose.camera.posematch.ui.common

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aipose.camera.posematch.R
import com.aipose.camera.posematch.ui.theme.poseTextStyle

private val BackGlyphSize = DpSize(10.dp, 17.dp)

@Composable
fun PoseBackHeader(
    title: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        PoseRaisedIconButton(
            iconRes = R.drawable.ic_pose_back,
            contentDescription = stringResource(R.string.action_back),
            glyphSize = BackGlyphSize,
            onClick = onBack,
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = title,
            style = poseTextStyle(22.sp, FontWeight.Bold, Color.White),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
