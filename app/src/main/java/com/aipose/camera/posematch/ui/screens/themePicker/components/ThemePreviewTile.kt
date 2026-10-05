package com.aipose.camera.posematch.ui.screens.themePicker.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.aipose.camera.posematch.ui.screens.themePicker.models.ThemePreviewStyle

private val TileSize = 80.dp
private val TileShape = RoundedCornerShape(18.dp)
private val SwatchShape = RoundedCornerShape(9.dp)
private val BarWidth = 40.dp
private val BarHeight = 7.dp
private val LineHeight = 5.dp
private val SwatchHeight = 28.dp
private const val TILE_BORDER_ALPHA = 0.12f

@Composable
internal fun ThemePreviewTile(
    style: ThemePreviewStyle,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .size(TileSize)
            .clip(TileShape)
            .background(style.background)
            .border(1.dp, Color.White.copy(alpha = TILE_BORDER_ALPHA), TileShape)
            .padding(start = 10.dp, end = 10.dp, top = 12.dp),
        verticalArrangement = Arrangement.spacedBy(7.dp),
    ) {
        Box(
            modifier = Modifier
                .width(BarWidth)
                .height(BarHeight)
                .clip(CircleShape)
                .background(style.bar)
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(LineHeight)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = style.lineAlpha))
        )
        Row(
            modifier = Modifier.padding(top = 5.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Box(
                modifier = Modifier
                    .weight(28f)
                    .height(SwatchHeight)
                    .clip(SwatchShape)
                    .background(style.swatch)
            )
            Box(
                modifier = Modifier
                    .weight(26f)
                    .height(SwatchHeight)
                    .clip(SwatchShape)
                    .background(Color.White.copy(alpha = style.tileAlpha))
            )
        }
    }
}
