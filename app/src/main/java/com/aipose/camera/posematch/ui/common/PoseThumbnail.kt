package com.aipose.camera.posematch.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aipose.camera.posematch.domain.models.Pose
import com.aipose.camera.posematch.ui.theme.LocalAppPalette

private val ThumbnailShape = RoundedCornerShape(8.dp)
private const val ThumbnailAspectRatio = 0.8f

@Composable
fun PoseThumbnail(
    pose: Pose,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = LocalAppPalette.current
    Box(
        modifier = modifier
            .aspectRatio(ThumbnailAspectRatio)
            .clip(ThumbnailShape)
            .border(0.5.dp, palette.glass, ThumbnailShape)
            .bounceClick(onClick = onClick)
    ) {
        PoseImage(
            imagePath = pose.imagePath,
            contentDescription = pose.title,
            modifier = Modifier.fillMaxSize(),
        )
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        listOf(Color.Transparent, Color.Black.copy(alpha = 0.75f))
                    )
                )
                .padding(horizontal = 8.dp, vertical = 6.dp)
        ) {
            Text(
                text = pose.title,
                color = Color.White,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.align(Alignment.BottomStart),
            )
        }
    }
}
