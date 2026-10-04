package com.aipose.camera.posematch.ui.screens.poseDetail.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.statusBarsPadding
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aipose.camera.posematch.R
import com.aipose.camera.posematch.ui.common.PoseImage
import com.aipose.camera.posematch.ui.common.bounceClick
import com.aipose.camera.posematch.ui.theme.PoseAmber
import com.aipose.camera.posematch.ui.theme.PosePinkSoft
import com.aipose.camera.posematch.ui.theme.PosePhotoScrim
import com.aipose.camera.posematch.ui.theme.poseTextStyle

private val ActionSize = 40.dp
private val ChipShape = RoundedCornerShape(15.dp)
private val SparkSize = 15.dp

private const val TOP_SCRIM_ALPHA = 0.6f
private const val TOP_SCRIM_STOP = 170f / 540f
private const val BOTTOM_SCRIM_STOP = 330f / 540f
private const val BOTTOM_SCRIM_ALPHA = 0.95f
private const val ACTION_FILL_ALPHA = 0.42f
private const val ACTION_BORDER_ALPHA = 0.2f
private const val BACK_GLOSS_ALPHA = 0.19f
private const val CHIP_FILL_ALPHA = 0.48f
private const val CHIP_BORDER_ALPHA = 0.2f

@Composable
internal fun PoseDetailHero(
    imagePath: String,
    category: String,
    isSaved: Boolean,
    onBack: () -> Unit,
    onToggleSaved: () -> Unit,
    onShare: () -> Unit,
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
        Row(
            modifier = Modifier
                .align(Alignment.TopStart)
                .statusBarsPadding()
                .fillMaxWidth()
                .padding(start = 20.dp, end = 20.dp, top = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            HeroAction(
                iconRes = R.drawable.ic_pose_back,
                contentDescription = stringResource(R.string.action_back),
                onClick = onBack,
                iconWidth = 10.dp,
                iconHeight = 17.dp,
                withGloss = true,
            )
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.End,
            ) {
                HeroAction(
                    iconRes = R.drawable.ic_pose_heart,
                    contentDescription = stringResource(
                        if (isSaved) R.string.saved_remove else R.string.saved_add
                    ),
                    onClick = onToggleSaved,
                    iconWidth = 15.dp,
                    iconHeight = 15.dp,
                    tint = if (isSaved) PosePinkSoft else Color.White,
                )
                HeroAction(
                    iconRes = R.drawable.ic_pose_share,
                    contentDescription = stringResource(R.string.action_share),
                    onClick = onShare,
                    iconWidth = 18.dp,
                    iconHeight = 19.dp,
                    modifier = Modifier.padding(start = 10.dp),
                )
            }
        }
        CategoryChip(
            category = category,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 24.dp, bottom = 90.dp),
        )
    }
}

@Composable
private fun HeroAction(
    @DrawableRes iconRes: Int,
    contentDescription: String,
    onClick: () -> Unit,
    iconWidth: Dp,
    iconHeight: Dp,
    modifier: Modifier = Modifier,
    tint: Color = Color.White,
    withGloss: Boolean = false,
) {
    Box(
        modifier = modifier
            .size(ActionSize)
            .clip(CircleShape)
            .background(Color.Black.copy(alpha = ACTION_FILL_ALPHA))
            .border(1.dp, Color.White.copy(alpha = ACTION_BORDER_ALPHA), CircleShape)
            .bounceClick(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        if (withGloss) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color.White.copy(alpha = BACK_GLOSS_ALPHA),
                                Color.Transparent,
                            )
                        )
                    ),
            )
        }
        Image(
            painter = painterResource(iconRes),
            contentDescription = contentDescription,
            colorFilter = ColorFilter.tint(tint),
            modifier = Modifier.size(width = iconWidth, height = iconHeight),
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
