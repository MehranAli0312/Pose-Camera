package com.aipose.camera.posematch.ui.screens.onboard.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.aipose.camera.posematch.ui.common.PoseScreenGutter
import com.aipose.camera.posematch.ui.common.adaptiveWidth
import com.aipose.camera.posematch.ui.common.safeBottomSystemBarsPadding
import com.aipose.camera.posematch.ui.common.safeTopSystemBarsPadding

@Composable
internal fun OnboardFrame(
    modifier: Modifier = Modifier,
    bottomReserved: Dp = 0.dp,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxHeight()
            .adaptiveWidth()
            .padding(bottom = bottomReserved)
            .consumeWindowInsets(PaddingValues(bottom = bottomReserved))
            .safeTopSystemBarsPadding()
            .safeBottomSystemBarsPadding()
            .padding(horizontal = PoseScreenGutter),
        horizontalAlignment = Alignment.CenterHorizontally,
        content = content,
    )
}
