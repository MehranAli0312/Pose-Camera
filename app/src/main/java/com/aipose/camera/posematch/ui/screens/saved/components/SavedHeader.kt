package com.aipose.camera.posematch.ui.screens.saved.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aipose.camera.posematch.R
import com.aipose.camera.posematch.ui.common.PoseRaisedIconButton
import com.aipose.camera.posematch.ui.common.PoseTopBar
import com.aipose.camera.posematch.ui.theme.LocalAppPalette
import com.aipose.camera.posematch.ui.theme.poseTextStyle

private val FilterGlyphSize = DpSize(15.dp, 14.dp)

@Composable
internal fun SavedHeader(
    totalCount: Int,
    onOpenSort: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        PoseTopBar(title = stringResource(R.string.nav_saved)) {
            PoseRaisedIconButton(
                iconRes = R.drawable.ic_pose_filter,
                contentDescription = stringResource(R.string.saved_sort_title),
                glyphSize = FilterGlyphSize,
                onClick = onOpenSort,
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = pluralStringResource(R.plurals.saved_subtitle, totalCount, totalCount),
            style = poseTextStyle(12.sp, FontWeight.Normal, LocalAppPalette.current.textMuted),
        )
    }
}
