package com.aipose.camera.posematch.ui.screens.pro.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aipose.camera.posematch.R
import com.aipose.camera.posematch.ui.common.bounceClick
import com.aipose.camera.posematch.ui.firebaseRemote.PremiumCloseButtonPosition

private val CloseButtonSize = 36.dp
private val CloseButtonTop = 14.dp
internal val ProTopBarHeight = CloseButtonTop + CloseButtonSize
private val CloseGlyphSize = 16.dp
private const val CLOSE_SURFACE_ALPHA = 0.08f
private const val CLOSE_BORDER_ALPHA = 0.12f

@Composable
internal fun ProTopBar(
    closeSecondsRemaining: Int,
    closePosition: PremiumCloseButtonPosition,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = CloseButtonTop, start = 18.dp, end = 18.dp),
        horizontalArrangement = when (closePosition) {
            PremiumCloseButtonPosition.Left -> Arrangement.Start
            PremiumCloseButtonPosition.Right -> Arrangement.End
        },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ProCloseButton(closeSecondsRemaining = closeSecondsRemaining, onClose = onClose)
    }
}

@Composable
private fun ProCloseButton(
    closeSecondsRemaining: Int,
    onClose: () -> Unit,
) {
    val canClose = closeSecondsRemaining <= 0
    val onBackground = MaterialTheme.colorScheme.onBackground
    Box(
        modifier = Modifier
            .size(CloseButtonSize)
            .background(onBackground.copy(alpha = CLOSE_SURFACE_ALPHA), CircleShape)
            .border(1.dp, onBackground.copy(alpha = CLOSE_BORDER_ALPHA), CircleShape)
            .then(if (canClose) Modifier.bounceClick(onClick = onClose) else Modifier),
        contentAlignment = Alignment.Center,
    ) {
        AnimatedContent(
            targetState = closeSecondsRemaining,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "proCloseCountdown",
        ) { seconds ->
            if (seconds > 0) {
                Text(
                    text = seconds.toString(),
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontSize = 14.sp,
                        lineHeight = 18.sp,
                        color = onBackground,
                    ),
                )
            } else {
                Icon(
                    painter = painterResource(R.drawable.ic_close),
                    contentDescription = null,
                    tint = onBackground,
                    modifier = Modifier.size(CloseGlyphSize),
                )
            }
        }
    }
}
