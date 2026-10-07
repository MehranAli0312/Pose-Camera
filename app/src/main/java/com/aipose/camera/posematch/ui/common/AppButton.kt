package com.aipose.camera.posematch.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.aipose.camera.posematch.ui.theme.BrandGradient

enum class IconPosition {
    Start, End
}

@Composable
fun AppButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    iconRes: Int? = null,
    iconPosition: IconPosition = IconPosition.Start,
    iconContentDescription: String? = null,
    useGradient: Boolean = true,
    gradientBrush: Brush = BrandGradient.brush,
    containerColor: Color = MaterialTheme.colorScheme.primary,
    contentColor: Color = MaterialTheme.colorScheme.onPrimary,
    cornerRadius: Dp = 50.dp,
    horizontalPadding: Dp = 24.dp,
    verticalPadding: Dp = 14.dp,
    iconSize: Dp = 20.dp,
    enabled: Boolean = true,
    textStyle: TextStyle? = null,
    trailingContent: (@Composable () -> Unit)? = null,
) {
    val shape = RoundedCornerShape(cornerRadius)
    val throttledOnClick = rememberThrottledClick(onClick)
    val clickableModifier = if (enabled) {
        Modifier.bounceClick(onClick = throttledOnClick)
    } else {
        Modifier
    }
    val backgroundModifier = if (useGradient) {
        Modifier.background(brush = gradientBrush, shape = shape)
    } else {
        Modifier.background(
            color = if (enabled) containerColor else containerColor.copy(alpha = 0.5f),
            shape = shape,
        )
    }

    Row(
        modifier = modifier
            .shadow(
                elevation = 8.dp,
                shape = shape,
                clip = false,
                spotColor = containerColor,
            )
            .clip(shape)
            .then(backgroundModifier)
            .alpha(if (enabled) 1f else 0.5f)
            .then(clickableModifier)
            .defaultMinSize(minHeight = 48.dp)
            .padding(horizontal = horizontalPadding, vertical = verticalPadding),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        if (iconRes != null && iconPosition == IconPosition.Start) {
            AppButtonIcon(
                iconRes = iconRes,
                contentDescription = iconContentDescription,
                tint = contentColor,
                size = iconSize,
            )
            Spacer(modifier = Modifier.width(8.dp))
        }

        Text(
            text = text,
            style = (textStyle ?: MaterialTheme.typography.labelLarge).copy(
                color = contentColor
            ),
        )

        if (iconRes != null && iconPosition == IconPosition.End) {
            Spacer(modifier = Modifier.width(8.dp))
            AppButtonIcon(
                iconRes = iconRes,
                contentDescription = iconContentDescription,
                tint = contentColor,
                size = iconSize,
            )
        }

        if (trailingContent != null) {
            Spacer(modifier = Modifier.width(8.dp))
            trailingContent()
        }
    }
}

@Composable
private fun AppButtonIcon(
    iconRes: Int,
    contentDescription: String?,
    tint: Color,
    size: Dp,
) {
    Icon(
        painter = painterResource(iconRes),
        contentDescription = contentDescription,
        tint = tint,
        modifier = Modifier.size(size),
    )
}
