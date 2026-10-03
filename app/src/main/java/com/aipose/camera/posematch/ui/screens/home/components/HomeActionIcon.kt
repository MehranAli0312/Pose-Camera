package com.aipose.camera.posematch.ui.screens.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.aipose.camera.posematch.ui.common.bounceClick
import com.aipose.camera.posematch.ui.theme.LocalAppPalette

private val ActionShape = RoundedCornerShape(14.dp)

@Composable
internal fun HomeActionIcon(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isAccent: Boolean = false,
) {
    val palette = LocalAppPalette.current
    Box(
        modifier = modifier
            .size(44.dp)
            .clip(ActionShape)
            .background(if (isAccent) palette.accent else palette.card)
            .border(1.dp, if (isAccent) Color.Transparent else palette.glass, ActionShape)
            .bounceClick(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = Color.White,
            modifier = Modifier.size(22.dp),
        )
    }
}
