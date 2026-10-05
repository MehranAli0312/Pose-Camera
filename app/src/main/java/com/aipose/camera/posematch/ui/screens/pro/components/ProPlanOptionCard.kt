package com.aipose.camera.posematch.ui.screens.pro.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aipose.camera.posematch.R
import com.aipose.camera.posematch.ui.common.bounceClick
import com.aipose.camera.posematch.domain.models.PremiumPlan
import com.aipose.camera.posematch.domain.models.ProPlan
import com.aipose.camera.posematch.ui.theme.AppMainColor
import com.aipose.camera.posematch.ui.theme.AppSecondaryColor
import com.aipose.camera.posematch.ui.theme.AppTheme
import com.aipose.camera.posematch.ui.theme.BrandGradient
import com.aipose.camera.posematch.ui.theme.brandGradientBackground

private val CardShape = RoundedCornerShape(20.dp)
private val ChipShape = RoundedCornerShape(10.dp)
private val BadgeHeight = 20.dp
private const val SELECTED_TINT_ALPHA = 0.08f
private const val UNAVAILABLE_ALPHA = 0.5f

private val SelectedTint = Brush.linearGradient(
    listOf(
        AppMainColor.copy(alpha = SELECTED_TINT_ALPHA),
        AppSecondaryColor.copy(alpha = SELECTED_TINT_ALPHA),
    ),
)

@Composable
internal fun ProPlanOptionCard(
    plan: ProPlan,
    details: PremiumPlan?,
    selected: Boolean,
    isLoading: Boolean,
    savePercent: Int?,
    showBestValue: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.fillMaxWidth()) {
        ProPlanOptionSurface(
            selected = selected,
            enabled = enabled,
            dimmed = !isLoading && details == null,
            onClick = onClick,
            modifier = Modifier.padding(top = if (showBestValue) BadgeHeight / 2 else 0.dp),
        ) {
            ProPlanRadio(selected = selected)
            Spacer(Modifier.width(14.dp))
            ProPlanDetails(
                plan = plan,
                details = details,
                isLoading = isLoading,
                savePercent = savePercent,
                modifier = Modifier.weight(1f),
            )
            Spacer(Modifier.width(12.dp))
            ProPlanPrice(details = details, selected = selected, isLoading = isLoading)
        }
        if (showBestValue) {
            ProBestValueBadge(modifier = Modifier.padding(start = 20.dp))
        }
    }
}

@Composable
private fun ProPlanOptionSurface(
    selected: Boolean,
    enabled: Boolean,
    dimmed: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit,
) {
    val colors = AppTheme.extendedColors
    val selectionModifier = if (selected) {
        Modifier
            .background(SelectedTint)
            .border(2.dp, BrandGradient.brush, CardShape)
    } else {
        Modifier.border(1.dp, colors.cardBorder, CardShape)
    }
    Row(
        modifier = modifier
            .fillMaxWidth()
            .alpha(if (dimmed) UNAVAILABLE_ALPHA else 1f)
            .clip(CardShape)
            .background(colors.glassSurface)
            .then(selectionModifier)
            .semantics {
                role = Role.RadioButton
                this.selected = selected
            }
            .bounceClick(enabled = enabled, onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        content = content,
    )
}

@Composable
private fun ProPlanRadio(selected: Boolean) {
    if (selected) {
        Box(
            modifier = Modifier
                .size(22.dp)
                .brandGradientBackground(CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_check_mark),
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(14.dp),
            )
        }
    } else {
        Box(
            modifier = Modifier
                .size(22.dp)
                .padding(1.dp)
                .border(2.dp, AppTheme.extendedColors.checkboxUnchecked, CircleShape),
        )
    }
}

@Composable
private fun ProPlanDetails(
    plan: ProPlan,
    details: PremiumPlan?,
    isLoading: Boolean,
    savePercent: Int?,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = proPlanTitle(plan),
                style = MaterialTheme.typography.titleMedium.copy(
                    fontSize = 16.sp,
                    lineHeight = 20.sp,
                    color = MaterialTheme.colorScheme.onBackground,
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f, fill = false),
            )
            savePercent?.let { percent ->
                Spacer(Modifier.width(8.dp))
                ProSaveChip(percent = percent)
            }
        }
        if (isLoading) {
            ProPricePlaceholder(width = 104.dp, height = 12.dp)
        } else {
            Text(
                text = proPlanSubtitle(details),
                style = MaterialTheme.typography.bodySmall.copy(
                    lineHeight = 16.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                ),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun ProPlanPrice(
    details: PremiumPlan?,
    selected: Boolean,
    isLoading: Boolean,
) {
    Column(horizontalAlignment = Alignment.End) {
        if (isLoading) {
            ProPricePlaceholder(width = 64.dp, height = 18.dp)
            return@Column
        }
        if (details == null) return@Column
        val monthlyPrice = rememberMonthlyPrice(details) ?: return@Column
        Text(
            text = monthlyPrice,
            style = MaterialTheme.typography.titleMedium.copy(
                fontSize = 18.sp,
                lineHeight = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                color = if (selected) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onBackground
                },
            ),
            maxLines = 1,
        )
        Text(
            text = stringResource(R.string.pro_per_month),
            style = MaterialTheme.typography.labelMedium.copy(
                fontSize = 12.sp,
                lineHeight = 16.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            ),
            maxLines = 1,
        )
    }
}

@Composable
private fun ProSaveChip(percent: Int) {
    val colors = AppTheme.extendedColors
    Text(
        text = stringResource(R.string.pro_save_percent, percent),
        style = MaterialTheme.typography.labelSmall.copy(
            fontSize = 10.sp,
            lineHeight = 12.sp,
            fontWeight = FontWeight.Bold,
            color = colors.onSoftAccentOrange,
        ),
        maxLines = 1,
        modifier = Modifier
            .clip(ChipShape)
            .background(colors.softAccentOrange)
            .padding(horizontal = 8.dp, vertical = 3.dp),
    )
}

@Composable
private fun ProBestValueBadge(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .height(BadgeHeight)
            .brandGradientBackground(ChipShape)
            .padding(horizontal = 10.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = stringResource(R.string.pro_best_value),
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 10.sp,
                lineHeight = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp,
                color = Color.White,
            ),
            maxLines = 1,
        )
    }
}

@Composable
private fun ProPricePlaceholder(width: Dp, height: Dp) {
    Box(
        modifier = Modifier
            .size(width = width, height = height)
            .clip(RoundedCornerShape(height / 2))
            .background(AppTheme.extendedColors.mutedBackground),
    )
}
