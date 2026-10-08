package com.aipose.camera.posematch.ui.screens.pro.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
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
import com.aipose.camera.posematch.domain.models.PremiumPlan
import com.aipose.camera.posematch.domain.models.ProPlan
import com.aipose.camera.posematch.ui.common.bounceClick
import com.aipose.camera.posematch.ui.theme.AppTheme
import com.aipose.camera.posematch.ui.theme.PosePremiumGold
import com.aipose.camera.posematch.ui.theme.PosePremiumGoldInk

private val CardShape = RoundedCornerShape(20.dp)
private val BadgeHeight = 22.dp
private val BadgeEndInset = 20.dp
private val CardPadding = 18.dp
private const val SELECTED_FILL_ALPHA = 0.1f
private const val CARD_FILL_ALPHA = 0.05f
private const val CARD_BORDER_ALPHA = 0.12f
private const val UNAVAILABLE_ALPHA = 0.5f

@Composable
internal fun ProPlanOptionCard(
    plan: ProPlan,
    details: PremiumPlan?,
    selected: Boolean,
    isLoading: Boolean,
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
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = proPlanTitle(plan),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontSize = 18.sp,
                        lineHeight = 23.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                Spacer(Modifier.width(12.dp))
                ProPlanPrice(
                    details = details,
                    selected = selected,
                    isLoading = isLoading,
                )
            }

            Spacer(Modifier.height(5.dp))

            if (isLoading) {
                ProPlanPlaceholder(width = 160.dp, height = 12.dp)
            } else {
                Text(
                    text = proPlanSubtitle(plan, details),
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 12.sp,
                        lineHeight = 15.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    ),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        if (showBestValue) {
            ProBestValueBadge(modifier = Modifier.align(Alignment.TopEnd))
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
    content: @Composable ColumnScope.() -> Unit,
) {
    val selectionModifier = if (selected) {
        Modifier
            .background(PosePremiumGold.copy(alpha = SELECTED_FILL_ALPHA), CardShape)
            .border(2.dp, PosePremiumGold, CardShape)
    } else {
        Modifier
            .background(
                MaterialTheme.colorScheme.onBackground.copy(alpha = CARD_FILL_ALPHA),
                CardShape,
            )
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = CARD_BORDER_ALPHA),
                shape = CardShape,
            )
    }
    Column(
        modifier = modifier
            .fillMaxWidth()
            .alpha(if (dimmed) UNAVAILABLE_ALPHA else 1f)
            .clip(CardShape)
            .then(selectionModifier)
            .semantics {
                role = Role.RadioButton
                this.selected = selected
            }
            .bounceClick(enabled = enabled, onClick = onClick)
            .padding(CardPadding),
        content = content,
    )
}

@Composable
private fun ProPlanPrice(
    details: PremiumPlan?,
    selected: Boolean,
    isLoading: Boolean,
) {
    Box(contentAlignment = Alignment.CenterEnd) {
        when {
            isLoading -> ProPlanPlaceholder(width = 84.dp, height = 20.dp)
            details == null -> Unit
            else -> Text(
                text = proPlanPrice(details),
                style = MaterialTheme.typography.titleMedium.copy(
                    fontSize = 20.sp,
                    lineHeight = 25.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (selected) {
                        PosePremiumGold
                    } else {
                        MaterialTheme.colorScheme.onBackground
                    },
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun ProBestValueBadge(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .padding(end = BadgeEndInset)
            .height(BadgeHeight)
            .clip(BadgeShape)
            .background(PosePremiumGold)
            .padding(horizontal = 11.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = stringResource(R.string.pro_best_value),
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 10.sp,
                lineHeight = 12.sp,
                fontWeight = FontWeight.Bold,
                color = PosePremiumGoldInk,
            ),
            maxLines = 1,
        )
    }
}

@Composable
private fun ProPlanPlaceholder(width: Dp, height: Dp) {
    Box(
        modifier = Modifier
            .size(width = width, height = height)
            .clip(RoundedCornerShape(height / 2))
            .background(AppTheme.extendedColors.mutedBackground),
    )
}
