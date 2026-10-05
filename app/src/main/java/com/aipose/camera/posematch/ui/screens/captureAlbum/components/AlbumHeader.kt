package com.aipose.camera.posematch.ui.screens.captureAlbum.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aipose.camera.posematch.R
import com.aipose.camera.posematch.ui.common.PoseBackButton
import com.aipose.camera.posematch.ui.common.PoseRaisedIconButton
import com.aipose.camera.posematch.ui.theme.LocalAppPalette
import com.aipose.camera.posematch.ui.theme.poseScreenTitleStyle
import com.aipose.camera.posematch.ui.theme.poseTextStyle

private val FilterGlyphSize = DpSize(18.dp, 17.dp)

@Composable
internal fun AlbumHeader(
    locationLabel: String,
    summary: String,
    onBack: () -> Unit,
    onOpenSort: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            PoseBackButton(onClick = onBack)
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = locationLabel,
                style = poseScreenTitleStyle(),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 12.dp),
            )
            PoseRaisedIconButton(
                iconRes = R.drawable.ic_pose_filter,
                contentDescription = stringResource(R.string.collections_sort_title),
                glyphSize = FilterGlyphSize,
                onClick = onOpenSort,
            )
        }
        Spacer(modifier = Modifier.height(18.dp))
        Text(
            text = summary,
            style = poseTextStyle(12.sp, FontWeight.Normal, LocalAppPalette.current.textMuted),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
