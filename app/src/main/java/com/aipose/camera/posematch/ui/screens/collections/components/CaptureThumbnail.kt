package com.aipose.camera.posematch.ui.screens.collections.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aipose.camera.posematch.R
import com.aipose.camera.posematch.ui.common.PoseImage
import com.aipose.camera.posematch.ui.common.bounceClick
import com.aipose.camera.posematch.ui.models.matchScoreColor
import com.aipose.camera.posematch.ui.screens.collections.models.CaptureUi
import com.aipose.camera.posematch.ui.theme.LocalAppPalette

private val ThumbnailShape = RoundedCornerShape(14.dp)

@Composable
internal fun CaptureThumbnail(
    captureUi: CaptureUi,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = LocalAppPalette.current
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .clip(ThumbnailShape)
            .border(0.5.dp, palette.glass, ThumbnailShape)
            .bounceClick(onClick = onClick)
    ) {
        PoseImage(
            imagePath = captureUi.capture.imagePath,
            contentDescription = captureUi.titleLabel,
            modifier = Modifier.fillMaxSize(),
        )

        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(6.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color.Black.copy(alpha = 0.55f))
                .padding(horizontal = 6.dp, vertical = 2.dp)
        ) {
            Text(
                text = stringResource(R.string.score_percent, captureUi.capture.matchScore),
                color = matchScoreColor(captureUi.capture.matchScore),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
            )
        }

        if (captureUi.capture.isFavorite) {
            Icon(
                imageVector = Icons.Default.Favorite,
                contentDescription = stringResource(R.string.action_favorite),
                tint = palette.accent,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(6.dp)
                    .size(16.dp),
            )
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        listOf(Color.Transparent, Color.Black.copy(alpha = 0.6f))
                    )
                )
                .padding(horizontal = 6.dp, vertical = 4.dp)
        ) {
            Text(
                text = captureUi.formattedShortDate,
                color = Color.White,
                fontSize = 9.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.align(Alignment.BottomStart),
            )
        }
    }
}
