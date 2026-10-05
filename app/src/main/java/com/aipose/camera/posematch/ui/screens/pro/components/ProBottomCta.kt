package com.aipose.camera.posematch.ui.screens.pro.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aipose.camera.posematch.R
import com.aipose.camera.posematch.ui.common.AppButton
import com.aipose.camera.posematch.ui.common.PrivacyNote
import com.aipose.camera.posematch.ui.common.bounceClick
import com.aipose.camera.posematch.ui.common.rememberThrottledClick
import com.aipose.camera.posematch.ui.theme.AppMainColor
import com.aipose.camera.posematch.ui.theme.White
import com.aipose.camera.posematch.ui.screens.pro.models.ProPlansState

@Composable
internal fun ProBottomCta(
    state: ProPlansState,
    isRestoring: Boolean,
    onContinue: () -> Unit,
    onRetry: () -> Unit,
    onPrivacyPolicy: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        ProBottomNote(state = state)

        Spacer(Modifier.height(12.dp))

        ProContinueButton(
            state = state,
            isRestoring = isRestoring,
            onClick = if (state is ProPlansState.Unavailable) onRetry else onContinue,
        )

        Spacer(Modifier.height(10.dp))

        ProLegalText(text = stringResource(R.string.pro_auto_renewal_note))

        Text(
            text = stringResource(R.string.settings_privacy_policy),
            style = MaterialTheme.typography.labelMedium.copy(
                fontSize = 12.sp,
                lineHeight = 16.sp,
                fontWeight = FontWeight.Medium,
                textDecoration = TextDecoration.Underline,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            ),
            modifier = Modifier
                .bounceClick(onClick = rememberThrottledClick(onPrivacyPolicy))
                .padding(horizontal = 8.dp, vertical = 4.dp),
        )
    }
}

@Composable
private fun ProBottomNote(state: ProPlansState) {
    when {
        state is ProPlansState.Unavailable -> ProLegalText(
            text = stringResource(R.string.pro_store_unavailable),
            color = MaterialTheme.colorScheme.error,
        )

        state is ProPlansState.Content && state.startsWithTrial -> PrivacyNote(
            text = stringResource(R.string.pro_no_payment_now),
        )

        else -> PrivacyNote(text = stringResource(R.string.pro_trust_note))
    }
}

@Composable
private fun ProContinueButton(
    state: ProPlansState,
    isRestoring: Boolean,
    onClick: () -> Unit,
) {
    val isPurchasing = state is ProPlansState.Content && state.isPurchasing
    val enabled = !isRestoring && when (state) {
        ProPlansState.Loading -> false
        ProPlansState.Unavailable -> true
        is ProPlansState.Content -> !state.isPurchasing
    }
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.CenterEnd,
    ) {
        AppButton(
            text = proCtaText(state),
            onClick = onClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            cornerRadius = 28.dp,
            verticalPadding = 0.dp,
            horizontalPadding = 56.dp,
            textStyle = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
            enabled = enabled,
        )
        Box(
            modifier = Modifier
                .padding(end = 8.dp)
                .size(40.dp)
                .clip(CircleShape)
                .background(White),
            contentAlignment = Alignment.Center,
        ) {
            if (isPurchasing || state is ProPlansState.Loading) {
                CircularProgressIndicator(
                    color = AppMainColor,
                    strokeWidth = 2.dp,
                    modifier = Modifier.size(20.dp),
                )
            } else {
                Icon(
                    painter = painterResource(R.drawable.ic_arrow_forward),
                    contentDescription = null,
                    tint = AppMainColor,
                    modifier = Modifier.size(24.dp),
                )
            }
        }
    }
}

@Composable
private fun ProLegalText(
    text: String,
    color: Color = MaterialTheme.colorScheme.onSurfaceVariant,
) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium.copy(
            fontSize = 11.sp,
            lineHeight = 15.sp,
            color = color,
        ),
        textAlign = TextAlign.Center,
    )
}
