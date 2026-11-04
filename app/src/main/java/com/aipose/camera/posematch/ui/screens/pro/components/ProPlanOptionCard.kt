package com.aipose.camera.posematch.ui.screens.pro.components

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
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
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

private val CardShape = RoundedCornerShape(18.dp)
private val ChipShape = RoundedCornerShape(50)
private val RadioSize = 24.dp
private val RadioCheckSize = 13.dp
private val RadioBorderWidth = 2.dp
private const val SELECTED_FILL_ALPHA = 0.08f
private const val CARD_FILL_ALPHA = 0.04f
private const val CARD_BORDER_ALPHA = 0.14f
private const val RADIO_BORDER_ALPHA = 0.45f
private const val UNAVAILABLE_ALPHA = 0.5f

@Composable
internal fun ProPlanOptionCard(
    plan: ProPlan,
    details: PremiumPlan?,
    selected: Boolean,
    isLoading: Boolean,
    badge: String?,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .alpha(if (!isLoading && details == null) UNAVAILABLE_ALPHA else 1f)
            .clip(CardShape)
            .then(planSurfaceModifier(selected))
            .semantics {
                role = Role.RadioButton
                this.selected = selected
            }
            .bounceClick(enabled = enabled, onClick = onClick)
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ProPlanRadio(selected = selected)
        Spacer(Modifier.width(14.dp))
        ProPlanInfo(
            plan = plan,
            details = details,
            isLoading = isLoading,
            modifier = Modifier.weight(1f),
        )
        Spacer(Modifier.width(12.dp))
        ProPlanPrice(
            details = details,
            selected = selected,
            isLoading = isLoading,
            badge = badge,
        )
    }
}

@Composable
private fun planSurfaceModifier(selected: Boolean): Modifier = if (selected) {
    Modifier
        .background(PosePremiumGold.copy(alpha = SELECTED_FILL_ALPHA), CardShape)
        .border(1.dp, PosePremiumGold, CardShape)
} else {
    Modifier
        .background(MaterialTheme.colorScheme.onBackground.copy(alpha = CARD_FILL_ALPHA), CardShape)
        .border(
            width = 1.dp,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = CARD_BORDER_ALPHA),
            shape = CardShape,
        )
}

@Composable
private fun ProPlanRadio(selected: Boolean) {
    if (selected) {
        Box(
            modifier = Modifier
                .size(RadioSize)
                .clip(CircleShape)
                .background(PosePremiumGold),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_check_mark),
                contentDescription = null,
                tint = PosePremiumGoldInk,
                modifier = Modifier.size(RadioCheckSize),
            )
        }
    } else {
        Box(
            modifier = Modifier
                .size(RadioSize)
                .border(
                    width = RadioBorderWidth,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = RADIO_BORDER_ALPHA),
                    shape = CircleShape,
                ),
        )
    }
}

@Composable
private fun ProPlanInfo(
    plan: ProPlan,
    details: PremiumPlan?,
    isLoading: Boolean,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Text(
            text = proPlanTitle(plan),
            style = MaterialTheme.typography.titleMedium.copy(
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
            ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )

        Spacer(Modifier.height(4.dp))

        if (isLoading) {
            ProPlanPlaceholder(width = 160.dp, height = 12.dp)
        } else {
            Text(
                text = proPlanSubtitle(plan, details),
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 12.sp,
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
    badge: String?,
) {
    Column(horizontalAlignment = Alignment.End) {
        when {
            isLoading -> ProPlanPlaceholder(width = 84.dp, height = 20.dp)
            details != null -> Text(
                text = proPlanPrice(details),
                style = MaterialTheme.typography.titleMedium.copy(
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (selected) PosePremiumGold else MaterialTheme.colorScheme.onBackground,
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        if (badge != null) {
            Spacer(Modifier.height(6.dp))
            ProPlanChip(text = badge)
        }
    }
}

@Composable
private fun ProPlanChip(text: String) {
    Box(
        modifier = Modifier
            .clip(ChipShape)
            .background(PosePremiumGold)
            .padding(horizontal = 10.dp, vertical = 3.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 10.sp,
                lineHeight = 13.sp,
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
