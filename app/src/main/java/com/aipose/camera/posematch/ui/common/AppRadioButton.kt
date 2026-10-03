package com.aipose.camera.posematch.ui.common

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.aipose.camera.posematch.R
import com.aipose.camera.posematch.ui.theme.AppMainColor
import com.aipose.camera.posematch.ui.theme.AppSecondaryColor

@Composable
fun AppRadioButton(
    checked: Boolean,
    onCheckedChange: () -> Unit,
    modifier: Modifier = Modifier,
    borderWidth: Dp = 1.5.dp,
    enabled: Boolean = true,
    checkmarkColor: Color = Color.White,
) {

    Box(
        modifier = modifier
            .size(20.dp)
            .clip(CircleShape)
            .background(
                if (checked) Brush.linearGradient(
                    colors = listOf(
                        AppMainColor,
                        AppSecondaryColor,
                    ),
                ) else Brush.linearGradient(
                    colors = listOf(
                        Color.Transparent,
                        Color.Transparent,
                    ),
                ),
                CircleShape
            )
            .border(
                borderWidth,
                if (!checked) Brush.linearGradient(
                    colors = listOf(
                        AppMainColor,
                        AppSecondaryColor,
                    ),
                ) else Brush.linearGradient(
                    colors = listOf(
                        Color.Transparent,
                        Color.Transparent,
                    ),
                ),
                CircleShape
            )
            .then(
                if (enabled) Modifier.bounceClick { onCheckedChange() } else Modifier,
            ),
        contentAlignment = Alignment.Center
    ) {
        AnimatedVisibility(checked) {
            Icon(
                painter = painterResource(R.drawable.ic_tick),
                contentDescription = null,
                tint = checkmarkColor,
            )
        }
    }
}
