package com.aipose.camera.posematch.ui.screens.home.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aipose.camera.posematch.R
import com.aipose.camera.posematch.ui.screens.pro.components.BADGE_SHADOW_ALPHA
import com.aipose.camera.posematch.ui.screens.pro.components.BadgeShape
import com.aipose.camera.posematch.ui.theme.PosePremiumGold
import com.aipose.camera.posematch.ui.theme.PosePremiumGoldDeep
import com.aipose.camera.posematch.ui.theme.PosePremiumGoldLight
import com.aipose.camera.posematch.ui.theme.PoseVioletLight
import com.aipose.camera.posematch.ui.theme.poseTextStyle

private val ProBadgeSize = 30.dp

@Composable
internal fun HomeHeader(
    showProBadge: Boolean,
    onProClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(end = 2.dp, bottom = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {

        val titleStyle = poseTextStyle(21.sp, FontWeight.Bold, Color.White)
        Text(
            modifier = Modifier.weight(1f),
            text = buildAnnotatedString {
                append(stringResource(R.string.home_title_pose))
                withStyle(titleStyle.toSpanStyle().copy(color = PoseVioletLight)) {
                    append(stringResource(R.string.home_title_ai))
                }
            },
            style = titleStyle,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )

        if (showProBadge) {
            Spacer(modifier = Modifier.width(8.dp))
            ProCrownBadgeHome(
                contentDescription = stringResource(R.string.premium_feature_go_premium_title),
                onClick = onProClick,
            )
        }
    }
}

@Composable
fun ProCrownBadgeHome(
    size: Dp = ProBadgeSize,
    contentDescription: String? = null,
    onClick: (() -> Unit)? = null,
) {
    Box(
        modifier = Modifier
            .size(size)
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
            modifier = Modifier.size(24.dp),
        )
    }
}
