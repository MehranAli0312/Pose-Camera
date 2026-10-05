package com.aipose.camera.posematch.ui.common

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aipose.camera.posematch.ui.models.GlossyBadgePalette
import com.aipose.camera.posematch.ui.theme.PoseTextBright
import com.aipose.camera.posematch.ui.theme.poseTextStyle

private val ChipMinHeight = 36.dp
private val ChipCorner = 18.dp
private val ChipShape = RoundedCornerShape(ChipCorner)
private val DefaultGlyphSize = 14.dp
private const val CHIP_BORDER_ALPHA = 0.1f
private const val SELECTED_SHADOW_ALPHA = 0.45f

@Composable
fun PoseFilterChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    @DrawableRes iconRes: Int? = null,
    iconTint: Color? = null,
    glyphSize: Dp = DefaultGlyphSize,
    selectedPalette: GlossyBadgePalette = GlossyBadgePalette.Violet,
) {
    val surface = if (isSelected) {
        Modifier.poseGradientPill(
            palette = selectedPalette,
            cornerRadius = ChipCorner,
            shadowAlpha = SELECTED_SHADOW_ALPHA,
            glossInsetX = 5.dp,
            glossTop = 3.dp,
            glossHeight = 16.dp,
        )
    } else {
        Modifier.poseRaisedSurface(ChipShape, borderAlpha = CHIP_BORDER_ALPHA)
    }
    Row(
        modifier = modifier
            .heightIn(min = ChipMinHeight)
            .then(surface)
            .semantics { selected = isSelected }
            .bounceClick(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        if (iconRes != null) {
            Image(
                painter = painterResource(iconRes),
                contentDescription = null,
                colorFilter = iconTint?.let { tint -> ColorFilter.tint(if (isSelected) Color.White else tint) },
                modifier = Modifier.size(glyphSize),
            )
        }
        Text(
            text = label,
            style = poseTextStyle(11.5.sp, FontWeight.Bold, if (isSelected) Color.White else PoseTextBright),
            maxLines = 1,
        )
    }
}
