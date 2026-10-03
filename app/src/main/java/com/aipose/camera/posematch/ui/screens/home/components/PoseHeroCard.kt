package com.aipose.camera.posematch.ui.screens.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aipose.camera.posematch.R
import com.aipose.camera.posematch.ui.common.PoseImage
import com.aipose.camera.posematch.ui.common.bounceClick
import com.aipose.camera.posematch.ui.theme.LocalAppPalette

@Composable
internal fun PoseHeroCard(
    heroImagePath: String?,
    onStartPosing: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val accent = LocalAppPalette.current.accent
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(128.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Brush.horizontalGradient(listOf(accent, accent.copy(alpha = 0.72f))))
    ) {
        if (heroImagePath != null) {
            PoseImage(
                imagePath = heroImagePath,
                contentDescription = null,
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .fillMaxHeight()
                    .width(150.dp),
            )
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(
                        Brush.horizontalGradient(
                            0f to accent,
                            0.55f to accent.copy(alpha = 0.85f),
                            1f to Color.Transparent,
                        )
                    )
            )
        }
        Column(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 18.dp, end = 12.dp, top = 14.dp, bottom = 14.dp)
        ) {
            Text(
                text = stringResource(R.string.hero_line1),
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = stringResource(R.string.hero_line2),
                color = Color.White,
                fontSize = 21.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = (-0.5).sp,
            )
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(Color.White)
                    .bounceClick(onClick = onStartPosing)
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.Default.PhotoCamera,
                    contentDescription = null,
                    tint = accent,
                    modifier = Modifier.size(16.dp),
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = stringResource(R.string.hero_cta),
                    color = accent,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp,
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = accent,
                    modifier = Modifier.size(14.dp),
                )
            }
        }
    }
}
