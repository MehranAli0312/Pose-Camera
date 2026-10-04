package com.aipose.camera.posematch.ui.common

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aipose.camera.posematch.R
import com.aipose.camera.posematch.domain.models.Pose
import com.aipose.camera.posematch.ui.theme.LocalAppPalette
import com.aipose.camera.posematch.ui.theme.PosePinkSoft

private val ThumbnailShape = RoundedCornerShape(8.dp)
private val HeartTouchSize = 30.dp
private val HeartGlyphSize = 12.dp
private const val ThumbnailAspectRatio = 0.8f
private const val HEART_FILL_ALPHA = 0.45f
private const val HEART_BORDER_ALPHA = 0.22f
private const val HEART_INACTIVE_ALPHA = 0.55f

@Composable
fun PoseThumbnail(
    pose: Pose,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isSaved: Boolean = false,
    onToggleSaved: (() -> Unit)? = null,
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
        if (onToggleSaved != null) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(4.dp)
                    .size(HeartTouchSize)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = HEART_FILL_ALPHA))
                    .border(0.5.dp, Color.White.copy(alpha = HEART_BORDER_ALPHA), CircleShape)
                    .click(onClick = onToggleSaved),
                contentAlignment = Alignment.Center,
            ) {
                Image(
                    painter = painterResource(R.drawable.ic_pose_heart),
                    contentDescription = stringResource(
                        if (isSaved) R.string.saved_remove else R.string.saved_add
                    ),
                    colorFilter = ColorFilter.tint(
                        if (isSaved) {
                            PosePinkSoft
                        } else {
                            Color.White.copy(alpha = HEART_INACTIVE_ALPHA)
                        }
                    ),
                    modifier = Modifier.size(HeartGlyphSize),
                )
            }
        }
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
