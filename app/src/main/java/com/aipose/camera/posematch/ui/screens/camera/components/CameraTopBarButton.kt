package com.aipose.camera.posematch.ui.screens.camera.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aipose.camera.posematch.ui.common.bounceClick
import com.aipose.camera.posematch.ui.theme.poseTextStyle

internal val CameraTopBarButtonSize = 40.dp
private val GlyphSize = 20.dp

@Composable
internal fun CameraTopBarButton(
    @DrawableRes iconRes: Int,
    tint: Color,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    badgeText: String? = null,
) {
    Box(
        modifier = modifier
            .size(CameraTopBarButtonSize)
            .bounceClick(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Image(
            painter = painterResource(iconRes),
            contentDescription = contentDescription,
            colorFilter = ColorFilter.tint(tint),
            modifier = Modifier.size(GlyphSize),
        )
        if (badgeText != null) {
            Text(
                text = badgeText,
                style = poseTextStyle(8.sp, FontWeight.Bold, tint),
                maxLines = 1,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 5.dp, bottom = 5.dp),
            )
        }
    }
}
