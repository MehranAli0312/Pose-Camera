package com.aipose.camera.posematch.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp

private const val LINE_HEIGHT_RATIO = 1.21f
private val ScreenTitleSize = 16.sp

@Composable
@ReadOnlyComposable
fun poseTextStyle(
    size: TextUnit,
    weight: FontWeight,
    color: Color
): TextStyle = TextStyle(
    fontFamily = appFontFamily,
    fontSize = size,
    fontWeight = weight,
    color = color,
    lineHeight = size * LINE_HEIGHT_RATIO
)

@Composable
@ReadOnlyComposable
fun poseScreenTitleStyle(
    color: Color = MaterialTheme.colorScheme.onSurface,
): TextStyle = MaterialTheme.typography.titleSmall.copy(
    fontSize = ScreenTitleSize,
    color = color,
)
