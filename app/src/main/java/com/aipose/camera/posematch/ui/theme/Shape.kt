package com.aipose.camera.posematch.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

val shapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(32.dp)
)

val topRound: RoundedCornerShape
    @Composable
    get() {
        return RoundedCornerShape(16.dp, 16.dp, 0.dp, 0.dp)
    }
fun commonShape() = RoundedCornerShape(12.dp)
