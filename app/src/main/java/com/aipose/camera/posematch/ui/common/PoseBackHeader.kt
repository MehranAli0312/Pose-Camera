package com.aipose.camera.posematch.ui.common

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.aipose.camera.posematch.ui.theme.poseScreenTitleStyle

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
        PoseBackButton(onClick = onBack)
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = title,
            style = poseScreenTitleStyle(),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
