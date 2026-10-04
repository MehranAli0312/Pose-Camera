package com.aipose.camera.posematch.ui.screens.onboard.components

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aipose.camera.posematch.R
import com.aipose.camera.posematch.ui.screens.onboard.data.ONBOARD_EXTRA_CATEGORY_COUNT
import com.aipose.camera.posematch.ui.screens.onboard.data.ONBOARD_POSE_COUNT
import com.aipose.camera.posematch.ui.theme.PoseAmber400
import com.aipose.camera.posematch.ui.theme.PoseFuchsiaLight
import com.aipose.camera.posematch.ui.theme.PoseRose400
import com.aipose.camera.posematch.ui.theme.PoseTextBright
import com.aipose.camera.posematch.ui.theme.PoseTextFaint
import com.aipose.camera.posematch.ui.theme.PoseVioletPale
import com.aipose.camera.posematch.ui.theme.Violet
import com.aipose.camera.posematch.ui.theme.poseTextStyle

private val CountBadgeStart = 210.dp
private val CountBadgeTop = 20.dp
private val SideCardWidth = 98.dp
private val SideCardHeight = 132.dp
private val SideCardTop = 84.dp
private val BeachCardStart = 22.dp
private val CoupleCardStart = 190.dp
private val HeroCardStart = 96.dp
private val HeroCardTop = 70.dp
private val HeroCardWidth = 118.dp
private val HeroCardHeight = 158.dp
private val HeroBorderStart = 94.dp
private val HeroBorderTop = 68.dp
private val HeroBorderWidth = 122.dp
private val HeroBorderHeight = 162.dp
private val HeroScrimHeight = 48.dp
private val HeroLabelStart = 12.dp
private val HeroLabelBottom = 10.dp
private val ViralBadgeStart = 60.dp
private val ViralBadgeTop = 10.dp
private val BadgeHeight = 26.dp
private val ViralBadgeHeight = 22.dp
private val ChipRowTop = 264.dp
private val ChipHeight = 30.dp
private val ChipIconSize = 11.dp
private val HintTop = 316.dp

private val SideCardShape = RoundedCornerShape(16.dp)
private val HeroCardShape = RoundedCornerShape(18.dp)
private val HeroBorderShape = RoundedCornerShape(20.dp)
private val CountBadgeShape = RoundedCornerShape(13.dp)
private val ChipShape = RoundedCornerShape(15.dp)
private val ViralBadgeShape = RoundedCornerShape(11.dp)

private const val COUNT_BADGE_FILL_ALPHA = 0.4f
private const val COUNT_BADGE_BORDER_ALPHA = 0.16f
private const val CHIP_FILL_ALPHA = 0.06f
private const val CHIP_BORDER_ALPHA = 0.08f
private const val MORE_CHIP_FILL_ALPHA = 0.16f
private const val MORE_CHIP_BORDER_ALPHA = 0.35f
private const val SCRIM_END_ALPHA = 0.78f

@Composable
internal fun OnboardPoseStage(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize()) {
        PoseCountBadge()
        SidePoseCard(BeachCardStart, R.drawable.onboard_pose_beach)
        SidePoseCard(CoupleCardStart, R.drawable.onboard_pose_couple)
        HeroPoseCard()
        CategoryChipRow()
        Text(
            text = stringResource(R.string.onboard_pose_hint),
            style = poseTextStyle(10.sp, FontWeight.Normal, PoseTextFaint),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = HintTop),
        )
    }
}

@Composable
private fun BoxScope.PoseCountBadge() {
    Box(
        modifier = Modifier
            .align(Alignment.TopStart)
            .offset(x = CountBadgeStart, y = CountBadgeTop)
            .height(BadgeHeight)
            .clip(CountBadgeShape)
            .background(Color.Black.copy(alpha = COUNT_BADGE_FILL_ALPHA))
            .border(1.dp, Color.White.copy(alpha = COUNT_BADGE_BORDER_ALPHA), CountBadgeShape)
            .padding(horizontal = 14.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = stringResource(R.string.onboard_pose_count_badge, ONBOARD_POSE_COUNT),
            style = poseTextStyle(9.sp, FontWeight.Bold, Color.White),
        )
    }
}

@Composable
private fun BoxScope.SidePoseCard(start: Dp, @DrawableRes imageRes: Int) {
    Image(
        painter = painterResource(imageRes),
        contentDescription = null,
        contentScale = ContentScale.Crop,
        modifier = Modifier
            .align(Alignment.TopStart)
            .offset(x = start, y = SideCardTop)
            .size(width = SideCardWidth, height = SideCardHeight)
            .clip(SideCardShape),
    )
}

@Composable
private fun BoxScope.HeroPoseCard() {
    Box(
        modifier = Modifier
            .align(Alignment.TopStart)
            .offset(x = HeroBorderStart, y = HeroBorderTop)
            .size(width = HeroBorderWidth, height = HeroBorderHeight)
            .border(2.dp, Violet, HeroBorderShape),
    )
    Box(
        modifier = Modifier
            .align(Alignment.TopStart)
            .offset(x = HeroCardStart, y = HeroCardTop)
            .size(width = HeroCardWidth, height = HeroCardHeight)
            .clip(HeroCardShape),
    ) {
        Image(
            painter = painterResource(R.drawable.onboard_pose_viral),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
        )
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(HeroScrimHeight)
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color.Transparent,
                            Color.Black.copy(alpha = SCRIM_END_ALPHA),
                        )
                    )
                ),
        )
        Text(
            text = stringResource(R.string.onboard_pose_name),
            style = poseTextStyle(11.sp, FontWeight.Bold, Color.White),
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = HeroLabelStart, bottom = HeroLabelBottom),
        )
    }
    Box(
        modifier = Modifier
            .align(Alignment.TopStart)
            .offset(
                x = HeroCardStart + ViralBadgeStart,
                y = HeroCardTop + ViralBadgeTop,
            )
            .height(ViralBadgeHeight)
            .clip(ViralBadgeShape)
            .background(Violet)
            .padding(horizontal = 11.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = stringResource(R.string.onboard_badge_viral),
            style = poseTextStyle(9.sp, FontWeight.Bold, Color.White),
        )
    }
}

@Composable
private fun BoxScope.CategoryChipRow() {
    Row(
        modifier = Modifier
            .align(Alignment.TopCenter)
            .offset(y = ChipRowTop),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CategoryChip(R.string.onboard_chip_viral, R.drawable.ic_pose_flame, PoseRose400)
        CategoryChip(R.string.onboard_chip_couple, R.drawable.ic_pose_couple, PoseFuchsiaLight)
        CategoryChip(R.string.onboard_chip_beach, R.drawable.ic_pose_sun, PoseAmber400)
        MoreChip()
    }
}

@Composable
private fun CategoryChip(@StringRes labelRes: Int, @DrawableRes iconRes: Int, iconTint: Color) {
    Row(
        modifier = Modifier
            .height(ChipHeight)
            .clip(ChipShape)
            .background(Color.White.copy(alpha = CHIP_FILL_ALPHA))
            .border(1.dp, Color.White.copy(alpha = CHIP_BORDER_ALPHA), ChipShape)
            .padding(horizontal = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Icon(
            painter = painterResource(iconRes),
            contentDescription = null,
            tint = iconTint,
            modifier = Modifier.size(ChipIconSize),
        )
        Text(
            text = stringResource(labelRes),
            style = poseTextStyle(9.5.sp, FontWeight.Bold, PoseTextBright),
        )
    }
}

@Composable
private fun MoreChip() {
    Box(
        modifier = Modifier
            .height(ChipHeight)
            .clip(ChipShape)
            .background(Violet.copy(alpha = MORE_CHIP_FILL_ALPHA))
            .border(1.dp, Violet.copy(alpha = MORE_CHIP_BORDER_ALPHA), ChipShape)
            .padding(horizontal = 12.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = stringResource(R.string.onboard_chip_more, ONBOARD_EXTRA_CATEGORY_COUNT),
            style = poseTextStyle(9.5.sp, FontWeight.Bold, PoseVioletPale),
        )
    }
}
