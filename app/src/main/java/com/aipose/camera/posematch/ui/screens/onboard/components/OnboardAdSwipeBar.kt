package com.aipose.camera.posematch.ui.screens.onboard.components

import androidx.compose.foundation.background
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.aipose.camera.posematch.R
import com.aipose.camera.posematch.ui.common.bounceClick

private val BarVerticalPadding = 4.dp
private val BarHorizontalPadding = 8.dp
private val ChevronTouchSize = 36.dp
private val ChevronIconSize = 14.dp
private const val PREVIOUS_CHEVRON_ROTATION = 180f
private const val NEXT_CHEVRON_ROTATION = 0f

@Composable
internal fun OnboardAdSwipeBar(
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(horizontal = BarHorizontalPadding, vertical = BarVerticalPadding),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SwipeBarChevron(
            rotation = PREVIOUS_CHEVRON_ROTATION,
            contentDescription = stringResource(R.string.onboard_ad_previous_cd),
            onClick = onPrevious,
        )
        Text(
            text = stringResource(R.string.onboard_ad_swipe_to_continue),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            maxLines = 1,
            modifier = Modifier.weight(1f),
        )
        SwipeBarChevron(
            rotation = NEXT_CHEVRON_ROTATION,
            contentDescription = stringResource(R.string.onboard_ad_next_cd),
            onClick = onNext,
        )
    }
}

@Composable
private fun SwipeBarChevron(rotation: Float, contentDescription: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(ChevronTouchSize)
            .clip(CircleShape)
            .bounceClick(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_pose_chevron_end),
            contentDescription = contentDescription,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .size(ChevronIconSize)
                .rotate(rotation),
        )
    }
}
