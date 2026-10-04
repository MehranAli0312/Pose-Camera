package com.aipose.camera.posematch.ui.screens.photoEdit.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aipose.camera.posematch.ui.common.bounceClick
import com.aipose.camera.posematch.ui.common.poseGradientPill
import com.aipose.camera.posematch.ui.common.poseRaisedSurface
import com.aipose.camera.posematch.ui.models.GlossyBadgePalette
import com.aipose.camera.posematch.ui.screens.photoEdit.models.CropAspect
import com.aipose.camera.posematch.ui.theme.LocalAppPalette
import com.aipose.camera.posematch.ui.theme.poseTextStyle

private val ChipHeight = 38.dp
private val ChipCorner = 19.dp
private val ChipShape = RoundedCornerShape(ChipCorner)
private const val CHIP_BORDER_ALPHA = 0.1f
private const val SELECTED_SHADOW_ALPHA = 0.45f

@Composable
internal fun CropAspectChips(
    selected: CropAspect,
    onAspectSelected: (CropAspect) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        CropAspect.entries.forEach { aspect ->
            val isSelected = aspect == selected
            val surface = if (isSelected) {
                Modifier.poseGradientPill(
                    palette = GlossyBadgePalette.Violet,
                    cornerRadius = ChipCorner,
                    shadowAlpha = SELECTED_SHADOW_ALPHA,
                    glossInsetX = 5.dp,
                    glossTop = 3.dp,
                    glossHeight = 16.dp,
                )
            } else {
                Modifier.poseRaisedSurface(ChipShape, borderAlpha = CHIP_BORDER_ALPHA)
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(ChipHeight)
                    .then(surface)
                    .bounceClick(onClick = { onAspectSelected(aspect) }),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = stringResource(aspect.labelRes),
                    style = poseTextStyle(
                        11.5.sp,
                        FontWeight.Bold,
                        if (isSelected) Color.White else LocalAppPalette.current.textMuted,
                    ),
                    maxLines = 1,
                )
            }
        }
    }
}
