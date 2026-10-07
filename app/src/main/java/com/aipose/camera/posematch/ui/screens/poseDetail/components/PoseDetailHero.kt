package com.aipose.camera.posematch.ui.screens.poseDetail.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.Image
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aipose.camera.posematch.R
import com.aipose.camera.posematch.ui.common.PoseImage
import com.aipose.camera.posematch.ui.theme.PoseAmber
import com.aipose.camera.posematch.ui.theme.PosePhotoScrim
import com.aipose.camera.posematch.ui.theme.poseTextStyle

private val ChipShape = RoundedCornerShape(15.dp)
private val SparkSize = 15.dp

private const val TOP_SCRIM_ALPHA = 0.6f
private const val TOP_SCRIM_STOP = 170f / 540f
private const val BOTTOM_SCRIM_STOP = 330f / 540f
private const val BOTTOM_SCRIM_ALPHA = 0.95f
private const val CHIP_FILL_ALPHA = 0.48f
private const val CHIP_BORDER_ALPHA = 0.2f

@Composable
internal fun PoseDetailHero(
    imagePath: String,
    category: String,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.fillMaxWidth()) {
        PoseImage(
            imagePath = imagePath,
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        0f to Color.Black.copy(alpha = TOP_SCRIM_ALPHA),
                        TOP_SCRIM_STOP to Color.Transparent,
                        BOTTOM_SCRIM_STOP to Color.Transparent,
                        1f to PosePhotoScrim.copy(alpha = BOTTOM_SCRIM_ALPHA),
                    )
                ),
        )
        CategoryChip(
            category = category,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 24.dp, bottom = 90.dp),
        )
    }
}

@Composable
private fun CategoryChip(category: String, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .height(30.dp)
            .clip(ChipShape)
            .background(Color.Black.copy(alpha = CHIP_FILL_ALPHA))
            .border(1.dp, Color.White.copy(alpha = CHIP_BORDER_ALPHA), ChipShape)
            .padding(start = 10.dp, end = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(7.dp),
    ) {
        Image(
            painter = painterResource(R.drawable.ic_pose_category_spark),
            contentDescription = null,
            colorFilter = ColorFilter.tint(PoseAmber),
            modifier = Modifier.size(SparkSize),
        )
        Text(
            text = category.uppercase(),
            style = poseTextStyle(10.5.sp, FontWeight.Bold, Color.White),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
