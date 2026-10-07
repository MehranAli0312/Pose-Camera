package com.aipose.camera.posematch.ui.screens.collections.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import com.aipose.camera.posematch.R
import com.aipose.camera.posematch.ui.common.PoseRaisedIconButton
import com.aipose.camera.posematch.ui.common.PoseTopBar

private val FilterGlyphSize = DpSize(15.dp, 14.dp)

@Composable
internal fun CollectionsHeader(
    onOpenSort: () -> Unit,
    modifier: Modifier = Modifier,
) {
    PoseTopBar(
        title = stringResource(R.string.nav_gallery),
        modifier = modifier,
    ) {
        PoseRaisedIconButton(
            iconRes = R.drawable.ic_pose_filter,
            contentDescription = stringResource(R.string.collections_sort_title),
            glyphSize = FilterGlyphSize,
            onClick = onOpenSort,
        )
    }
}
