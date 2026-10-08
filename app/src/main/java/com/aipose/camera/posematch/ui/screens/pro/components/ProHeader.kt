package com.aipose.camera.posematch.ui.screens.pro.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aipose.camera.posematch.R
import com.aipose.camera.posematch.ui.theme.PosePremiumGold
import com.aipose.camera.posematch.ui.theme.PosePremiumGoldDeep
import com.aipose.camera.posematch.ui.theme.PosePremiumGoldLight

private val BadgeSize = 88.dp
private const val OUTER_HALO_RADIUS_RATIO = 120f / 88f
private const val INNER_HALO_RADIUS_RATIO = 84f / 88f
val BadgeShape = RoundedCornerShape(28.dp)
private const val OUTER_HALO_ALPHA = 0.04f
private const val INNER_HALO_ALPHA = 0.07f
const val BADGE_SHADOW_ALPHA = 0.45f

@Composable
internal fun ProHeader(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        ProCrownBadge()

        Spacer(Modifier.height(22.dp))

        Text(
            text = rememberProTitle(),
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 26.sp,
                lineHeight = 33.sp,
                color = MaterialTheme.colorScheme.onBackground,
            ),
            textAlign = TextAlign.Center,
        )

        Spacer(Modifier.height(5.dp))

        Text(
            text = stringResource(R.string.pro_subtitle),
            style = MaterialTheme.typography.bodyMedium.copy(
                fontSize = 14.sp,
                lineHeight = 18.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            ),
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
        )
    }
}

@Composable
private fun rememberProTitle(): AnnotatedString {
    val title = stringResource(R.string.pro_title)
    val highlight = stringResource(R.string.pro_title_highlight)
    return remember(title, highlight) {
        buildAnnotatedString {
            append(title)
            val start = title.indexOf(highlight)
            if (start >= 0) {
                addStyle(
                    style = SpanStyle(color = PosePremiumGold),
                    start = start,
                    end = start + highlight.length,
                )
            }
        }
    }
}

@Composable
fun ProCrownBadge(
    size: Dp = BadgeSize,
    contentDescription: String? = null,
    onClick: (() -> Unit)? = null,
) {
    Box(
        modifier = Modifier
            .size(size)
            .drawBehind {
                drawCircle(
                    color = PosePremiumGold.copy(alpha = OUTER_HALO_ALPHA),
                    radius = this.size.minDimension * OUTER_HALO_RADIUS_RATIO,
                )
                drawCircle(
                    color = PosePremiumGold.copy(alpha = INNER_HALO_ALPHA),
                    radius = this.size.minDimension * INNER_HALO_RADIUS_RATIO,
                )
            },
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .shadow(
                    elevation = 16.dp,
                    shape = BadgeShape,
                    ambientColor = PosePremiumGold.copy(alpha = BADGE_SHADOW_ALPHA),
                    spotColor = PosePremiumGold.copy(alpha = BADGE_SHADOW_ALPHA),
                )
                .clip(BadgeShape)
                .then(
                    if (onClick != null) {
                        Modifier.clickable(role = Role.Button, onClick = onClick)
                    } else {
                        Modifier
                    },
                )
                .background(
                    Brush.verticalGradient(
                        listOf(PosePremiumGoldLight, PosePremiumGoldDeep),
                    ),
                ),
            contentAlignment = Alignment.Center,
        ) {

            Image(
                painter = painterResource(R.drawable.ic_pro_crown_premium),
                contentDescription = contentDescription,
                modifier = Modifier.size(size),
            )
        }
    }
}
