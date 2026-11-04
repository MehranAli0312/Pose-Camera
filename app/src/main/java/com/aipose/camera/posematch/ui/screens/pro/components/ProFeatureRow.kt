package com.aipose.camera.posematch.ui.screens.pro.components

import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aipose.camera.posematch.ui.screens.pro.models.ProFeature
import com.aipose.camera.posematch.ui.screens.pro.models.proPaywallFeatures
import com.aipose.camera.posematch.ui.theme.PosePremiumGold

private val FeatureIconSize = 24.dp
private val DividerHeight = 44.dp
private const val DIVIDER_ALPHA = 0.18f

@Composable
internal fun ProFeatureRow(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        proPaywallFeatures.forEachIndexed { index, feature ->
            if (index > 0) ProFeatureDivider()
            ProFeatureItem(feature = feature, modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun ProFeatureItem(feature: ProFeature, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(horizontal = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            painter = painterResource(feature.iconRes),
            contentDescription = null,
            tint = PosePremiumGold,
            modifier = Modifier.size(FeatureIconSize),
        )
        Spacer(Modifier.height(8.dp))
        Text(
            modifier = Modifier.basicMarquee(iterations = Int.MAX_VALUE),
            text = stringResource(feature.labelRes),
            style = MaterialTheme.typography.bodyMedium.copy(
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onBackground,
            ),
            textAlign = TextAlign.Center,
            maxLines = 1,
        )
    }
}

@Composable
private fun ProFeatureDivider() {
    Box(
        modifier = Modifier
            .width(1.dp)
            .height(DividerHeight)
            .background(MaterialTheme.colorScheme.onBackground.copy(alpha = DIVIDER_ALPHA)),
    )
}
