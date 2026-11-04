package com.aipose.camera.posematch.ui.screens.pro.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.aipose.camera.posematch.R
import com.aipose.camera.posematch.ui.common.bounceClick
import com.aipose.camera.posematch.ui.firebaseRemote.PremiumCloseButtonPosition

private val CloseButtonSize = 30.dp
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
            .padding(start = 18.dp, end = 18.dp),
        horizontalArrangement = when (closePosition) {
            PremiumCloseButtonPosition.Left -> Arrangement.Start
            PremiumCloseButtonPosition.Right -> Arrangement.End
        },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AnimatedVisibility(
            visible = closeSecondsRemaining <= 0,
            enter = fadeIn(),
            label = "proCloseButton",
        ) {
            ProCloseButton(onClose = onClose)
        }
    }
}

@Composable
private fun ProCloseButton(onClose: () -> Unit) {
    val onBackground = MaterialTheme.colorScheme.onBackground
    Box(
        modifier = Modifier
            .size(CloseButtonSize)
            .background(onBackground.copy(alpha = CLOSE_SURFACE_ALPHA), CircleShape)
            .border(1.dp, onBackground.copy(alpha = CLOSE_BORDER_ALPHA), CircleShape)
            .bounceClick(onClick = onClose),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_close),
            contentDescription = null,
            tint = onBackground,
            modifier = Modifier.size(CloseGlyphSize),
        )
    }
}
