package com.aipose.camera.posematch.ui.screens.pro.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aipose.camera.posematch.R
import com.aipose.camera.posematch.ui.screens.pro.models.proPaywallBenefits
import com.aipose.camera.posematch.ui.theme.PosePremiumGold
import com.aipose.camera.posematch.ui.theme.PosePremiumGoldInk

private val CardShape = RoundedCornerShape(20.dp)
private val CardPadding = 18.dp
private val RowSpacing = 8.dp
private val CheckSize = 20.dp
private val CheckGlyphSize = 12.dp
private val CheckGap = 12.dp
private const val CARD_FILL_ALPHA = 0.05f
private const val CARD_BORDER_ALPHA = 0.1f

@Composable
internal fun ProBenefitsCard(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(CardShape)
            .background(MaterialTheme.colorScheme.onBackground.copy(alpha = CARD_FILL_ALPHA))
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = CARD_BORDER_ALPHA),
                shape = CardShape,
            )
            .padding(CardPadding),
        verticalArrangement = Arrangement.spacedBy(RowSpacing),
    ) {
        proPaywallBenefits.forEach { benefitRes ->
            ProBenefitRow(text = stringResource(benefitRes))
        }
    }
}

@Composable
private fun ProBenefitRow(text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(CheckSize)
                .clip(CircleShape)
                .background(PosePremiumGold),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_check_mark),
                contentDescription = null,
                tint = PosePremiumGoldInk,
                modifier = Modifier.size(CheckGlyphSize),
            )
        }
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontSize = 15.sp,
                lineHeight = 18.sp,
                color = MaterialTheme.colorScheme.onBackground,
            ),
            modifier = Modifier.padding(start = CheckGap),
        )
    }
}
