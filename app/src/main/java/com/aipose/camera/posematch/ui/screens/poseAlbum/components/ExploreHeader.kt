package com.aipose.camera.posematch.ui.screens.poseAlbum.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aipose.camera.posematch.R
import com.aipose.camera.posematch.ui.common.PoseBackButton
import com.aipose.camera.posematch.ui.theme.PoseVioletPale
import com.aipose.camera.posematch.ui.theme.Violet
import com.aipose.camera.posematch.ui.theme.poseScreenTitleStyle
import com.aipose.camera.posematch.ui.theme.poseTextStyle

private val TotalShape = RoundedCornerShape(14.dp)
private const val TOTAL_FILL_ALPHA = 0.22f

@Composable
internal fun ExploreHeader(
    totalCount: Int,
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
            text = stringResource(R.string.explore_title),
            style = poseScreenTitleStyle(),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = stringResource(R.string.explore_total, totalCount),
            style = poseTextStyle(11.sp, FontWeight.Bold, PoseVioletPale),
            maxLines = 1,
            modifier = Modifier
                .padding(start = 8.dp)
                .clip(TotalShape)
                .background(Violet.copy(alpha = TOTAL_FILL_ALPHA))
                .padding(horizontal = 11.dp, vertical = 7.dp),
        )
    }
}
