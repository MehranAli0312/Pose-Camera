package com.aipose.camera.posematch.ui.screens.bottomSheet.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aipose.camera.posematch.R
import com.aipose.camera.posematch.ui.common.bounceClick
import com.aipose.camera.posematch.ui.theme.AppMainColor
import com.aipose.camera.posematch.ui.theme.AppSecondaryColor
import com.aipose.camera.posematch.ui.theme.AppTheme
import com.aipose.camera.posematch.ui.theme.FixLuxuryGold
import com.aipose.camera.posematch.ui.theme.brandGradientBackground

private val OptionShape = RoundedCornerShape(20.dp)
private val BadgeShape = RoundedCornerShape(14.dp)
private val OptionHeight = 68.dp
private val BadgeSize = 44.dp
private val TrailingSize = 28.dp
private val ProgressStroke = 2.5.dp
private const val LOADING_BORDER_ALPHA = 0.45f
private const val PREMIUM_BADGE_ALPHA = 0.22f
private const val PREMIUM_SUBTITLE_ALPHA = 0.85f
private const val DISABLED_ALPHA = 0.5f

@Composable
internal fun FreeAccessOption(
    isLoading: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val borderColor = if (isLoading) {
        AppMainColor.copy(alpha = LOADING_BORDER_ALPHA)
    } else {
        AppTheme.extendedColors.cardBorder
    }
    val softAccent = AppTheme.extendedColors.softAccent
    OptionRow(
        modifier = modifier
            .clip(OptionShape)
            .background(MaterialTheme.colorScheme.surface, OptionShape)
            .border(1.dp, borderColor, OptionShape)
            .bounceClick(enabled = !isLoading, onClick = onClick),
        badge = {
            OptionBadge(
                iconRes = R.drawable.ic_premium_watch_ad,
                background = softAccent,
                iconSize = 24.dp,
                tint = Color.Unspecified,
            )
        },
        title = stringResource(R.string.premium_feature_free_access_title),
        subtitle = stringResource(
            if (isLoading) R.string.premium_feature_free_access_loading
            else R.string.premium_feature_free_access_subtitle,
        ),
        titleColor = MaterialTheme.colorScheme.onBackground,
        subtitleColor = MaterialTheme.colorScheme.onSurfaceVariant,
        trailing = {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(TrailingSize - 4.dp),
                    color = AppMainColor,
                    trackColor = softAccent,
                    strokeWidth = ProgressStroke,
                )
            } else {
                ForwardArrow(background = softAccent, tint = AppMainColor)
            }
        },
    )
}

@Composable
internal fun GoPremiumOption(
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    OptionRow(
        modifier = modifier
            .alpha(if (enabled) 1f else DISABLED_ALPHA)
            .clip(OptionShape)
            .brandGradientBackground(OptionShape)
            .bounceClick(enabled = enabled, onClick = onClick),
        badge = {
            OptionBadge(
                iconRes = R.drawable.ic_pro_glyph_crown,
                background = Color.White.copy(alpha = PREMIUM_BADGE_ALPHA),
                iconSize = 22.dp,
                tint = FixLuxuryGold,
            )
        },
        title = stringResource(R.string.premium_feature_go_premium_title),
        subtitle = stringResource(R.string.premium_feature_go_premium_subtitle),
        titleColor = Color.White,
        subtitleColor = Color.White.copy(alpha = PREMIUM_SUBTITLE_ALPHA),
        trailing = { ForwardArrow(background = Color.White, tint = AppSecondaryColor) },
    )
}

@Composable
private fun OptionRow(
    badge: @Composable () -> Unit,
    title: String,
    subtitle: String,
    titleColor: Color,
    subtitleColor: Color,
    trailing: @Composable () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(OptionHeight)
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        badge()
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontSize = 16.sp,
                    lineHeight = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = titleColor,
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = subtitle,
                modifier = Modifier.padding(top = 4.dp),
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 12.sp,
                    lineHeight = 15.sp,
                    fontWeight = FontWeight.Normal,
                    color = subtitleColor,
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Box(
            modifier = Modifier.size(TrailingSize),
            contentAlignment = Alignment.Center,
        ) {
            trailing()
        }
        Spacer(modifier = Modifier.width(2.dp))
    }
}

@Composable
private fun OptionBadge(
    @DrawableRes iconRes: Int,
    background: Color,
    iconSize: Dp,
    tint: Color,
) {
    Box(
        modifier = Modifier
            .size(BadgeSize)
            .background(background, BadgeShape),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painter = painterResource(iconRes),
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(iconSize),
        )
    }
}

@Composable
private fun ForwardArrow(background: Color, tint: Color) {
    Box(
        modifier = Modifier
            .size(TrailingSize)
            .background(background, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_arrow_forward),
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(20.dp),
        )
    }
}
