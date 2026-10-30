package com.aipose.camera.posematch.ui.screens.onboard.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.aipose.camera.posematch.R
import com.aipose.camera.posematch.ads.OnboardingFullScreenNative
import com.aipose.camera.posematch.ui.common.bounceClick
import com.aipose.camera.posematch.ui.common.safeBottomSystemBarsPadding
import com.aipose.camera.posematch.ui.common.safeTopSystemBarsPadding
import com.example.ads.AdPlacement
import com.example.ads.compose.AdsPreparedSlot
import kotlinx.coroutines.delay

private val CloseButtonSize = 28.dp
private val CloseIconSize = 12.dp
private val CloseButtonInset = 10.dp
private const val MILLIS_PER_SECOND = 1000L

@Composable
internal fun OnboardNativeAdPage(
    isSettled: Boolean,
    closeDelaySeconds: Int,
    onClose: () -> Unit,
    onPrevious: () -> Unit,
    modifier: Modifier = Modifier,
    bottomReserved: Dp = 0.dp,
) {
    var closeVisible by remember { mutableStateOf(false) }

    LaunchedEffect(isSettled, closeDelaySeconds) {
        if (!isSettled || closeVisible) return@LaunchedEffect
        delay(closeDelaySeconds * MILLIS_PER_SECOND)
        closeVisible = true
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(bottom = bottomReserved)
            .safeTopSystemBarsPadding()
            .safeBottomSystemBarsPadding(),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
        ) {
            AdsPreparedSlot(
                placement = AdPlacement.OnboardingFullScreenNative,
                modifier = Modifier.fillMaxSize(),
                keepUntilReleased = true,
            )
            if (closeVisible) {
                OnboardAdCloseButton(
                    onClick = onClose,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(start = CloseButtonInset, top = CloseButtonInset),
                )
            }
        }
        OnboardAdSwipeBar(
            onPrevious = onPrevious,
            onNext = onClose,
        )
    }
}

@Composable
private fun OnboardAdCloseButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(CloseButtonSize)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.secondaryContainer)
            .bounceClick(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_close),
            contentDescription = stringResource(R.string.close),
            tint = MaterialTheme.colorScheme.onSecondaryContainer,
            modifier = Modifier.size(CloseIconSize),
        )
    }
}
