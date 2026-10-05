package com.aipose.camera.posematch.ui.screens.bottomSheet

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import com.aipose.camera.posematch.ui.common.safeBottomSystemBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aipose.camera.posematch.R
import com.aipose.camera.posematch.ui.common.FloatingIcon3D
import com.aipose.camera.posematch.ui.common.ImmersiveDialogWindowEffect
import com.aipose.camera.posematch.ui.common.bounceClick
import com.aipose.camera.posematch.ui.models.Icon3DPalette
import com.aipose.camera.posematch.ui.screens.bottomSheet.components.FreeAccessOption
import com.aipose.camera.posematch.ui.screens.bottomSheet.components.GoPremiumOption
import com.aipose.camera.posematch.ui.theme.PremiumFeatureAccent
import com.aipose.camera.posematch.ui.theme.topRound

private val HeroSize = 112.dp
private val HeroIconSize = 76.dp
private val HeroBoxSize = 100.dp
private const val HERO_GLOW_ALPHA = 0.35f
private const val TAG_BACKGROUND_ALPHA = 0.12f
private const val DISABLED_ALPHA = 0.5f

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PremiumFeatureDialog(
    isAdLoading: Boolean,
    onWatchAdClick: () -> Unit,
    onGoPremiumClick: () -> Unit,
    onDismissRequest: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
        confirmValueChange = { !isAdLoading },
    )

    ModalBottomSheet(
        onDismissRequest = { if (!isAdLoading) onDismissRequest() },
        sheetState = sheetState,
        shape = topRound,
        containerColor = MaterialTheme.colorScheme.background,
    ) {
        ImmersiveDialogWindowEffect()
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .safeBottomSystemBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
                .padding(bottom = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            PremiumCrownHero()

            PremiumFeatureTag(modifier = Modifier.padding(top = 2.dp))

            Text(
                text = stringResource(R.string.premium_feature_title),
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = MaterialTheme.colorScheme.onBackground,
                ),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 10.dp),
            )

            Text(
                text = stringResource(R.string.premium_feature_message),
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 14.sp,
                    lineHeight = 18.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                ),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 6.dp, start = 8.dp, end = 8.dp),
            )

            Spacer(modifier = Modifier.height(20.dp))

            FreeAccessOption(
                isLoading = isAdLoading,
                onClick = onWatchAdClick,
            )

            Spacer(modifier = Modifier.height(12.dp))

            GoPremiumOption(
                enabled = !isAdLoading,
                onClick = onGoPremiumClick,
            )

            Text(
                text = stringResource(R.string.premium_feature_maybe_later),
                style = MaterialTheme.typography.titleSmall.copy(
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                ),
                modifier = Modifier
                    .padding(top = 8.dp)
                    .alpha(if (isAdLoading) DISABLED_ALPHA else 1f)
                    .bounceClick(enabled = !isAdLoading, onClick = onDismissRequest)
                    .padding(horizontal = 24.dp, vertical = 12.dp),
            )
        }
    }
}

@Composable
private fun PremiumCrownHero() {
    val glow = PremiumFeatureAccent.copy(alpha = HERO_GLOW_ALPHA)
    Box(
        modifier = Modifier
            .size(HeroSize)
            .drawBehind {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(glow, Color.Transparent),
                        center = center,
                        radius = size.minDimension / 2f,
                    ),
                )
            },
        contentAlignment = Alignment.Center,
    ) {
        FloatingIcon3D(
            iconRes = R.drawable.ic_pro_glyph_crown,
            palette = Icon3DPalette.Indigo,
            iconSize = HeroIconSize,
            boxSize = HeroBoxSize,
            sparkleTint = MaterialTheme.colorScheme.primary,
        )
    }
}

@Composable
private fun PremiumFeatureTag(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .background(PremiumFeatureAccent.copy(alpha = TAG_BACKGROUND_ALPHA), CircleShape)
            .padding(horizontal = 10.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_pro_glyph_crown),
            contentDescription = null,
            tint = PremiumFeatureAccent,
            modifier = Modifier.size(14.dp),
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = stringResource(R.string.premium_feature_tag),
            style = MaterialTheme.typography.labelMedium.copy(
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = PremiumFeatureAccent,
            ),
            maxLines = 1,
        )
    }
}
