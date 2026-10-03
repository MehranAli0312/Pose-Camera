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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aipose.camera.posematch.R
import com.aipose.camera.posematch.ui.common.bounceClick
import com.aipose.camera.posematch.ui.common.rememberThrottledClick
import com.aipose.camera.posematch.ui.firebaseRemote.PremiumCloseButtonPosition
import com.aipose.camera.posematch.ui.theme.AppTheme

@Composable
internal fun ProTopBar(
    closeSecondsRemaining: Int,
    closePosition: PremiumCloseButtonPosition,
    onClose: () -> Unit,
    canRestore: Boolean,
    onRestore: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 12.dp, start = 16.dp, end = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        when (closePosition) {
            PremiumCloseButtonPosition.Left -> {
                ProCloseButton(closeSecondsRemaining = closeSecondsRemaining, onClose = onClose)
                ProRestoreText(enabled = canRestore, onClick = onRestore)
            }

            PremiumCloseButtonPosition.Right -> {
                ProRestoreText(enabled = canRestore, onClick = onRestore)
                ProCloseButton(closeSecondsRemaining = closeSecondsRemaining, onClose = onClose)
            }
        }
    }
}

@Composable
private fun ProRestoreText(
    enabled: Boolean,
    onClick: () -> Unit,
) {
    val colors = AppTheme.extendedColors
    Text(
        text = stringResource(R.string.pro_restore),
        modifier = Modifier
            .alpha(if (enabled) ENABLED_ALPHA else DISABLED_ALPHA)
            .clip(PillShape)
            .background(colors.glassSurface)
            .border(1.dp, colors.glassBorder, PillShape)
            .bounceClick(enabled = enabled, onClick = rememberThrottledClick(onClick))
            .padding(horizontal = 16.dp, vertical = 8.dp),
        style = MaterialTheme.typography.labelLarge.copy(
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            lineHeight = 18.sp,
            color = MaterialTheme.colorScheme.onBackground,
        ),
    )
}

@Composable
private fun ProCloseButton(
    closeSecondsRemaining: Int,
    onClose: () -> Unit,
) {
    val canClose = closeSecondsRemaining <= 0
    Box(
        modifier = Modifier
            .size(34.dp)
            .clip(CircleShape)
            .background(AppTheme.extendedColors.glassSurface)
            .border(1.dp, AppTheme.extendedColors.glassBorder, CircleShape)
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
                        fontSize = 12.sp,
                        lineHeight = 16.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    ),
                )
            } else {
                Icon(
                    painter = painterResource(R.drawable.ic_close),
                    contentDescription = stringResource(R.string.close),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp),
                )
            }
        }
    }
}

private val PillShape = RoundedCornerShape(17.dp)
private const val ENABLED_ALPHA = 1f
private const val DISABLED_ALPHA = 0.5f
