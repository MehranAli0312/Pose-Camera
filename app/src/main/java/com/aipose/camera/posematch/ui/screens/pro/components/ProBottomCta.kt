package com.aipose.camera.posematch.ui.screens.pro.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aipose.camera.posematch.R
import com.aipose.camera.posematch.ui.common.AppButton
import com.aipose.camera.posematch.ui.common.bounceClick
import com.aipose.camera.posematch.ui.common.rememberThrottledClick
import com.aipose.camera.posematch.ui.screens.pro.models.ProPlansState
import com.aipose.camera.posematch.ui.theme.Indigo
import com.aipose.camera.posematch.ui.theme.PosePremiumCtaDeep

private val CtaHeight = 56.dp
private val CtaCornerRadius = 28.dp
private val FooterDotSize = 3.dp
private const val FOOTNOTE_ALPHA = 0.6f
private const val FOOTER_LINK_ALPHA = 0.8f
private const val FOOTER_DOT_ALPHA = 0.35f

private val CtaBrush = Brush.verticalGradient(listOf(Indigo, PosePremiumCtaDeep))

@Composable
internal fun ProBottomCta(
    state: ProPlansState,
    isRestoring: Boolean,
    canRestore: Boolean,
    onContinue: () -> Unit,
    onRetry: () -> Unit,
    onRestore: () -> Unit,
    onTerms: () -> Unit,
    onPrivacyPolicy: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        ProContinueButton(
            state = state,
            isRestoring = isRestoring,
            onClick = if (state is ProPlansState.Unavailable) onRetry else onContinue,
        )

        Spacer(Modifier.height(16.dp))

        Text(
            text = proFootnote(state),
            style = MaterialTheme.typography.bodySmall.copy(
                fontSize = 12.sp,
                lineHeight = 15.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = FOOTNOTE_ALPHA),
            ),
            textAlign = TextAlign.Center,
        )

        Spacer(Modifier.height(26.dp))

        ProFooterLinks(
            canRestore = canRestore,
            onRestore = onRestore,
            onTerms = onTerms,
            onPrivacyPolicy = onPrivacyPolicy,
        )
    }
}

@Composable
private fun ProContinueButton(
    state: ProPlansState,
    isRestoring: Boolean,
    onClick: () -> Unit,
) {
    val isPurchasing = state is ProPlansState.Content && state.isPurchasing
    val isBusy = isPurchasing || state is ProPlansState.Loading
    val enabled = !isRestoring && !isBusy
    AppButton(
        text = proCtaText(state),
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(CtaHeight),
        gradientBrush = CtaBrush,
        cornerRadius = CtaCornerRadius,
        verticalPadding = 0.dp,
        textStyle = MaterialTheme.typography.titleMedium.copy(
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
        ),
        enabled = enabled,
        trailingContent = if (isBusy) {
            {
                CircularProgressIndicator(
                    color = Color.White,
                    strokeWidth = 2.dp,
                    modifier = Modifier.size(18.dp),
                )
            }
        } else {
            null
        },
    )
}

@Composable
private fun ProFooterLinks(
    canRestore: Boolean,
    onRestore: () -> Unit,
    onTerms: () -> Unit,
    onPrivacyPolicy: () -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        ProFooterLink(
            text = stringResource(R.string.pro_restore),
            enabled = canRestore,
            onClick = onRestore,
        )
        ProFooterDot()
        ProFooterLink(text = stringResource(R.string.pro_terms), onClick = onTerms)
        ProFooterDot()
        ProFooterLink(text = stringResource(R.string.pro_privacy), onClick = onPrivacyPolicy)
    }
}

@Composable
private fun ProFooterLink(
    text: String,
    onClick: () -> Unit,
    enabled: Boolean = true,
) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium.copy(
            fontSize = 12.sp,
            lineHeight = 15.sp,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = FOOTER_LINK_ALPHA),
        ),
        modifier = Modifier
            .alpha(if (enabled) 1f else FOOTER_DOT_ALPHA)
            .bounceClick(enabled = enabled, onClick = rememberThrottledClick(onClick))
            .padding(vertical = 4.dp),
    )
}

@Composable
private fun ProFooterDot() {
    Box(
        modifier = Modifier
            .size(FooterDotSize)
            .clip(CircleShape)
            .background(
                MaterialTheme.colorScheme.onBackground.copy(alpha = FOOTER_DOT_ALPHA),
            ),
    )
}
