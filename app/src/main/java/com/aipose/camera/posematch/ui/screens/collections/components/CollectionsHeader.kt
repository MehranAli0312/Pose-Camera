package com.aipose.camera.posematch.ui.screens.collections.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aipose.camera.posematch.R
import com.aipose.camera.posematch.ui.common.PoseRaisedIconButton
import com.aipose.camera.posematch.ui.theme.poseTextStyle

private val FilterGlyphSize = DpSize(18.dp, 17.dp)

@Composable
internal fun CollectionsHeader(
    onOpenSort: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(R.string.nav_gallery),
            style = poseTextStyle(26.sp, FontWeight.Bold, Color.White),
            modifier = Modifier.weight(1f),
        )
        PoseRaisedIconButton(
            iconRes = R.drawable.ic_pose_filter,
            contentDescription = stringResource(R.string.collections_sort_title),
            glyphSize = FilterGlyphSize,
            onClick = onOpenSort,
        )
    }
}
