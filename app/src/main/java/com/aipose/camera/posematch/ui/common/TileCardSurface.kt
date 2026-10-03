package com.aipose.camera.posematch.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.aipose.camera.posematch.R
import com.aipose.camera.posematch.ui.theme.AppTheme

@Composable
fun Modifier.tileCardSurface(shape: Shape): Modifier {
    val shadowColor = AppTheme.extendedColors.contactShadow
    return this
        .shadow(
            elevation = 10.dp,
            shape = shape,
            ambientColor = shadowColor,
            spotColor = shadowColor,
        )
        .clip(shape)
        .background(MaterialTheme.colorScheme.secondaryContainer)
}

@Composable
fun TileArrowBadge(
    tint: Color,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(28.dp)
            .clip(CircleShape)
            .background(tint.copy(alpha = 0.14f)),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_arrow_forward),
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(14.dp),
        )
    }
}
