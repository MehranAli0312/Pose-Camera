package com.aipose.camera.posematch.ui.common

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private const val TAG_BACKGROUND_ALPHA = 0.1f

@Composable
fun StatusTag(
    @DrawableRes iconRes: Int,
    text: String,
    modifier: Modifier = Modifier,
    tint: Color = MaterialTheme.colorScheme.error,
) {
    Row(
        modifier = modifier
            .clip(CircleShape)
            .background(tint.copy(alpha = TAG_BACKGROUND_ALPHA))
            .padding(start = 8.dp, end = 12.dp, top = 5.dp, bottom = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            painter = painterResource(iconRes),
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier
                .size(16.dp)
                .clip(CircleShape)
                .background(tint)
                .padding(3.dp),
        )
        Text(
            text = text,
            modifier = Modifier.padding(start = 6.dp),
            style = MaterialTheme.typography.labelMedium.copy(
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = tint,
            ),
            maxLines = 1,
        )
    }
}
