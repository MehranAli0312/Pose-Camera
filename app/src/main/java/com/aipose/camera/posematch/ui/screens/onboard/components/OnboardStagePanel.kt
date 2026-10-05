package com.aipose.camera.posematch.ui.screens.onboard.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.unit.dp
import com.aipose.camera.posematch.ui.common.fitDesignSize
import com.aipose.camera.posematch.ui.common.poseRaisedCard
import com.aipose.camera.posematch.ui.theme.PoseShadow
import com.aipose.camera.posematch.ui.theme.PoseSurface

internal val StagePanelWidth = 310.dp
internal val StagePanelHeight = 356.dp

private val StageCorner = 28.dp
private const val STAGE_SHADOW_ALPHA = 0.45f
private const val STAGE_BORDER_ALPHA = 0.09f

@Composable
internal fun OnboardStagePanel(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    Box(
        modifier = modifier
            .fitDesignSize(StagePanelWidth, StagePanelHeight)
            .poseRaisedCard(
                cornerRadius = StageCorner,
                brush = SolidColor(PoseSurface),
                shadowColor = PoseShadow,
                shadowAlpha = STAGE_SHADOW_ALPHA,
                borderColor = Color.White.copy(alpha = STAGE_BORDER_ALPHA),
            ),
        content = content,
    )
}
