package com.aipose.camera.posematch.ui.screens.collections.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.aipose.camera.posematch.R
import com.aipose.camera.posematch.ui.common.PoseTopBar

@Composable
internal fun CollectionsHeader(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    PoseTopBar(
        title = stringResource(R.string.nav_gallery),
        onBack = onBack,
        modifier = modifier,
    )
}
