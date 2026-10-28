package com.aipose.camera.posematch.ui.screens.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aipose.camera.posematch.R
import com.aipose.camera.posematch.ui.screens.pro.components.BADGE_SHADOW_ALPHA
import com.aipose.camera.posematch.ui.theme.PosePremiumGold
import com.aipose.camera.posematch.ui.theme.PosePremiumGoldDeep
import com.aipose.camera.posematch.ui.theme.PosePremiumGoldInk
import com.aipose.camera.posematch.ui.theme.PosePremiumGoldLight
import com.aipose.camera.posematch.ui.theme.poseTextStyle
import kotlinx.coroutines.delay

private val BadgeHeight = 30.dp
private val CrownSize = 17.dp
private val LabelSize = 12.5.sp

private const val LETTER_DELAY_MS = 220L
private const val FULL_WORD_HOLD_MS = 1_400L
private const val EMPTY_WORD_HOLD_MS = 300L

@Composable
internal fun HomeProBadge(
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .height(BadgeHeight)
            .shadow(
                elevation = 16.dp,
                shape = CircleShape,
                ambientColor = PosePremiumGold.copy(alpha = BADGE_SHADOW_ALPHA),
                spotColor = PosePremiumGold.copy(alpha = BADGE_SHADOW_ALPHA),
            )
            .clip(CircleShape)
            .clickable(onClick = onClick)
            .background(Brush.verticalGradient(listOf(PosePremiumGoldLight, PosePremiumGoldDeep)))
            .clearAndSetSemantics {
                this.contentDescription = contentDescription
                role = Role.Button
            }
            .padding(start = 10.dp, end = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp),
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_pro_crown_premium),
            contentDescription = null,
            tint = PosePremiumGoldInk,
            modifier = Modifier.size(CrownSize),
        )
        TypingLabel(text = stringResource(R.string.pose_pro_badge))
    }
}

@Composable
private fun TypingLabel(text: String) {
    var typedCount by remember(text) { mutableIntStateOf(0) }

    LaunchedEffect(text) {
        while (true) {
            for (count in 1..text.length) {
                typedCount = count
                delay(LETTER_DELAY_MS)
            }
            delay(FULL_WORD_HOLD_MS)
            typedCount = 0
            delay(EMPTY_WORD_HOLD_MS)
        }
    }

    Text(
        text = buildAnnotatedString {
            append(text.take(typedCount))
            withStyle(SpanStyle(color = Color.Transparent)) {
                append(text.drop(typedCount))
            }
        },
        style = poseTextStyle(LabelSize, FontWeight.ExtraBold, PosePremiumGoldInk),
        maxLines = 1,
    )
}
