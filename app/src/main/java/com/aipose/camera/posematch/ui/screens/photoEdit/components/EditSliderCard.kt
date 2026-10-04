package com.aipose.camera.posematch.ui.screens.photoEdit.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aipose.camera.posematch.ui.common.poseRaisedSurface
import com.aipose.camera.posematch.ui.theme.LocalAppPalette
import com.aipose.camera.posematch.ui.theme.poseTextStyle

private const val CARD_GLOSS_ALPHA = 0.16f

@Composable
internal fun EditSliderCard(
    label: String,
    value: String,
    valueColor: Color,
    cornerRadius: Dp,
    modifier: Modifier = Modifier,
    valueSize: TextUnit = 12.sp,
    hasGloss: Boolean = true,
    verticalPadding: Dp = 14.dp,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .poseRaisedSurface(
                shape = RoundedCornerShape(cornerRadius),
                glossAlpha = if (hasGloss) CARD_GLOSS_ALPHA else 0f,
            )
            .padding(horizontal = 16.dp, vertical = verticalPadding),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = label,
                style = poseTextStyle(8.5.sp, FontWeight.Bold, LocalAppPalette.current.textFaint),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = value,
                style = poseTextStyle(valueSize, FontWeight.Bold, valueColor),
                maxLines = 1,
            )
        }
        content()
    }
}
