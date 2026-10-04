package com.aipose.camera.posematch.ui.screens.onboard.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aipose.camera.posematch.R
import com.aipose.camera.posematch.ui.screens.onboard.data.ONBOARD_SAVED_MATCH_PERCENT
import com.aipose.camera.posematch.ui.theme.PoseEmerald400
import com.aipose.camera.posematch.ui.theme.PoseEmerald600
import com.aipose.camera.posematch.ui.theme.PoseFilterCoolBottom
import com.aipose.camera.posematch.ui.theme.PoseFilterMonoBottom
import com.aipose.camera.posematch.ui.theme.PoseFilterMonoTop
import com.aipose.camera.posematch.ui.theme.PoseFilterPearlBottom
import com.aipose.camera.posematch.ui.theme.PoseFilterPearlTop
import com.aipose.camera.posematch.ui.theme.PoseFilterWarmBottom
import com.aipose.camera.posematch.ui.theme.PoseOrangeLight
import com.aipose.camera.posematch.ui.theme.PoseSkyLight
import com.aipose.camera.posematch.ui.theme.PoseTextBright
import com.aipose.camera.posematch.ui.theme.PoseVioletPale
import com.aipose.camera.posematch.ui.theme.poseTextStyle

private val PreviewStart = 39.dp
private val PreviewTop = 28.dp
private val PreviewWidth = 232.dp
private val PreviewHeight = 300.dp
private val PreviewShape = RoundedCornerShape(20.dp)
private val MatchBadgeStart = 55.dp
private val MatchBadgeTop = 48.dp
private val MatchBadgeHeight = 28.dp
private val MatchBadgeShape = RoundedCornerShape(14.dp)
private val FiltersLabelStart = 55.dp
private val FiltersLabelTop = 221.5.dp
private val SwatchRowStart = 64.dp
private val SwatchRowTop = 236.dp
private val SwatchSize = 32.dp
private val SwatchSlot = 40.dp
private val SwatchSpacing = 4.dp
private val SwatchRingSize = 40.dp
private val SavedPillStart = 55.dp
private val SavedPillTop = 286.dp
private val SavedPillHeight = 36.dp
private val SavedPillShape = RoundedCornerShape(18.dp)
private val SavedBubbleSize = 20.dp
private val CheckIconWidth = 12.dp
private val CheckIconHeight = 10.dp

private const val SAVED_FILL_ALPHA = 0.55f
private const val SAVED_BORDER_ALPHA = 0.16f
private const val SWATCH_GRADIENT_SKEW = 0.4f

private val EmeraldStops = listOf(PoseEmerald400, PoseEmerald600)

private val FilterSwatches = listOf(
    PoseFilterPearlTop to PoseFilterPearlBottom,
    PoseOrangeLight to PoseFilterWarmBottom,
    PoseSkyLight to PoseFilterCoolBottom,
    PoseFilterMonoTop to PoseFilterMonoBottom,
)

@Composable
internal fun OnboardSaveStage(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize()) {
        Image(
            painter = painterResource(R.drawable.onboard_captured_shot),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset(x = PreviewStart, y = PreviewTop)
                .size(width = PreviewWidth, height = PreviewHeight)
                .clip(PreviewShape),
        )
        MatchBadge()
        Text(
            text = stringResource(R.string.onboard_filters_label),
            style = poseTextStyle(8.5.sp, FontWeight.Bold, PoseTextBright),
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset(x = FiltersLabelStart, y = FiltersLabelTop),
        )
        FilterSwatchRow()
        SavedPill()
    }
}

@Composable
private fun BoxScope.MatchBadge() {
    Row(
        modifier = Modifier
            .align(Alignment.TopStart)
            .offset(x = MatchBadgeStart, y = MatchBadgeTop)
            .height(MatchBadgeHeight)
            .clip(MatchBadgeShape)
            .background(diagonalGradient(EmeraldStops, MatchBadgeHeight))
            .padding(horizontal = 11.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(7.dp),
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_pose_check_small),
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(width = CheckIconWidth, height = CheckIconHeight),
        )
        Text(
            text = stringResource(R.string.onboard_match_badge, ONBOARD_SAVED_MATCH_PERCENT),
            style = poseTextStyle(9.5.sp, FontWeight.Bold, Color.White),
        )
    }
}

@Composable
private fun BoxScope.FilterSwatchRow() {
    Row(
        modifier = Modifier
            .align(Alignment.TopStart)
            .offset(x = SwatchRowStart, y = SwatchRowTop),
        horizontalArrangement = Arrangement.spacedBy(SwatchSpacing),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        FilterSwatches.forEachIndexed { index, stops ->
            Box(
                modifier = Modifier.size(SwatchSlot),
                contentAlignment = Alignment.Center,
            ) {
                if (index == 0) {
                    Box(
                        modifier = Modifier
                            .size(SwatchRingSize)
                            .border(2.dp, PoseVioletPale, CircleShape),
                    )
                }
                Box(
                    modifier = Modifier
                        .size(SwatchSize)
                        .clip(CircleShape)
                        .background(diagonalGradient(stops.toList(), SwatchSize)),
                )
            }
        }
    }
}

@Composable
private fun BoxScope.SavedPill() {
    Row(
        modifier = Modifier
            .align(Alignment.TopStart)
            .offset(x = SavedPillStart, y = SavedPillTop)
            .height(SavedPillHeight)
            .clip(SavedPillShape)
            .background(Color.Black.copy(alpha = SAVED_FILL_ALPHA))
            .border(1.dp, Color.White.copy(alpha = SAVED_BORDER_ALPHA), SavedPillShape)
            .padding(horizontal = 9.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Box(
            modifier = Modifier
                .size(SavedBubbleSize)
                .clip(CircleShape)
                .background(diagonalGradient(EmeraldStops, SavedBubbleSize)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_pose_check_small),
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(width = CheckIconWidth, height = CheckIconHeight),
            )
        }
        Text(
            text = stringResource(R.string.onboard_saved_label),
            style = poseTextStyle(11.sp, FontWeight.Bold, Color.White),
        )
    }
}

@Composable
private fun diagonalGradient(colors: List<Color>, height: Dp): Brush {
    val extent = with(LocalDensity.current) { height.toPx() }
    return Brush.linearGradient(
        colors = colors,
        start = Offset.Zero,
        end = Offset(extent * SWATCH_GRADIENT_SKEW, extent),
    )
}
