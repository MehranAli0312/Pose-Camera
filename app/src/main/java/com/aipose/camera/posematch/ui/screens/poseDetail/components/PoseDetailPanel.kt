package com.aipose.camera.posematch.ui.screens.poseDetail.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aipose.camera.posematch.R
import com.aipose.camera.posematch.ui.common.bounceClick
import com.aipose.camera.posematch.ui.common.poseGradientPill
import com.aipose.camera.posematch.ui.models.GlossyBadgePalette
import com.aipose.camera.posematch.ui.screens.poseDetail.models.PoseDetailUiState
import com.aipose.camera.posematch.ui.theme.LocalAppPalette
import com.aipose.camera.posematch.ui.theme.PoseAmber
import com.aipose.camera.posematch.ui.theme.PoseChipBottom
import com.aipose.camera.posematch.ui.theme.PoseChipLabel
import com.aipose.camera.posematch.ui.theme.PoseChipTop
import com.aipose.camera.posematch.ui.theme.PoseCyanBright
import com.aipose.camera.posematch.ui.theme.PoseGlowPurple
import com.aipose.camera.posematch.ui.theme.PoseSheetBottom
import com.aipose.camera.posematch.ui.theme.PoseSheetTop
import com.aipose.camera.posematch.ui.theme.poseTextStyle
import com.aipose.camera.posematch.util.bidiIsolate

private val PanelShape = RoundedCornerShape(topStart = 34.dp, topEnd = 34.dp)
private val HandleWidth = 56.dp
private val HandleHeight = 5.dp
private val ChipShape = RoundedCornerShape(14.dp)
private val ChipHeight = 28.dp
private val ChipDotSize = 6.8.dp
private val MatchCardShape = RoundedCornerShape(20.dp)
private val MatchCardHeight = 60.dp
private val RingSize = 40.dp
private val RingStroke = 4.dp
private val CtaHeight = 58.dp
private val CtaCorner = 29.dp

private const val PANEL_BORDER_ALPHA = 0.12f
private const val PANEL_GLOSS_ALPHA = 0.12f
private const val HANDLE_ALPHA = 0.28f
private const val CHIP_BORDER_ALPHA = 0.1f
private const val RING_TRACK_ALPHA = 0.14f
private const val RING_START_ANGLE = -90f
private const val FULL_SWEEP = 360f
private const val PERCENT_SCALE = 100f

@Composable
internal fun PoseDetailPanel(
    content: PoseDetailUiState.Content,
    onStartPosing: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = LocalAppPalette.current
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(PanelShape)
            .background(Brush.verticalGradient(listOf(PoseSheetTop, PoseSheetBottom)))
            .border(1.dp, Color.White.copy(alpha = PANEL_BORDER_ALPHA), PanelShape)
            .padding(horizontal = 24.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 18.dp),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                modifier = Modifier
                    .width(HandleWidth)
                    .height(HandleHeight)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = HANDLE_ALPHA)),
            )
        }
        Spacer(modifier = Modifier.height(17.dp))
        Text(
            text = content.pose.title,
            style = poseTextStyle(24.sp, FontWeight.Bold, Color.White),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
        Spacer(modifier = Modifier.height(11.dp))
        InfoChips(content = content)
        Spacer(modifier = Modifier.height(15.dp))
        Text(
            text = content.pose.description,
            style = poseTextStyle(12.5.sp, FontWeight.Normal, palette.textMuted),
        )
        Spacer(modifier = Modifier.height(20.dp))
        BestMatchCard(bestMatch = content.bestMatch)
        Spacer(modifier = Modifier.height(18.dp))
        StartPosingButton(onClick = onStartPosing)
        Spacer(modifier = Modifier.height(24.dp))
        Spacer(modifier = Modifier.navigationBarsPadding())
    }
}

@Composable
private fun InfoChips(content: PoseDetailUiState.Content, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        InfoChip(label = content.pose.difficulty, dotColor = PoseAmber)
        InfoChip(
            label = stringResource(R.string.pose_detail_in_set, content.categoryCount),
        )
        InfoChip(
            label = pluralStringResource(
                R.plurals.pose_detail_tries,
                content.tryCount,
                content.tryCount,
            ),
        )
    }
}

@Composable
private fun InfoChip(
    label: String,
    modifier: Modifier = Modifier,
    dotColor: Color? = null,
) {
    Row(
        modifier = modifier
            .height(ChipHeight)
            .clip(ChipShape)
            .background(Brush.verticalGradient(listOf(PoseChipTop, PoseChipBottom)))
            .border(1.dp, Color.White.copy(alpha = CHIP_BORDER_ALPHA), ChipShape)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp),
    ) {
        if (dotColor != null) {
            Box(
                modifier = Modifier
                    .size(ChipDotSize)
                    .clip(CircleShape)
                    .background(dotColor),
            )
        }
        Text(
            text = label,
            style = poseTextStyle(10.5.sp, FontWeight.Bold, PoseChipLabel),
            maxLines = 1,
        )
    }
}

@Composable
private fun BestMatchCard(bestMatch: Int?, modifier: Modifier = Modifier) {
    val palette = LocalAppPalette.current
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(MatchCardHeight)
            .clip(MatchCardShape)
            .background(Brush.verticalGradient(listOf(PoseChipTop, PoseChipBottom)))
            .border(1.dp, Color.White.copy(alpha = CHIP_BORDER_ALPHA), MatchCardShape)
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(modifier = Modifier.size(RingSize), contentAlignment = Alignment.Center) {
            MatchRing(percent = bestMatch ?: 0)
            Text(
                text = stringResource(R.string.score_percent, bestMatch ?: 0).bidiIsolate(),
                style = poseTextStyle(9.5.sp, FontWeight.Bold, Color.White),
            )
        }
        Column(modifier = Modifier.padding(start = 18.dp)) {
            Text(
                text = stringResource(
                    if (bestMatch == null) {
                        R.string.pose_detail_no_match
                    } else {
                        R.string.pose_detail_best_match
                    }
                ),
                style = poseTextStyle(12.5.sp, FontWeight.Bold, Color.White),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = stringResource(
                    if (bestMatch == null) {
                        R.string.pose_detail_no_match_sub
                    } else {
                        R.string.pose_detail_best_match_sub
                    }
                ),
                style = poseTextStyle(10.sp, FontWeight.Normal, palette.textMuted),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun MatchRing(percent: Int, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.size(RingSize)) {
        val stroke = RingStroke.toPx()
        val inset = stroke / 2f
        val arcSize = Size(size.width - stroke, size.height - stroke)
        drawArc(
            color = Color.White.copy(alpha = RING_TRACK_ALPHA),
            startAngle = 0f,
            sweepAngle = FULL_SWEEP,
            useCenter = false,
            topLeft = Offset(inset, inset),
            size = arcSize,
            style = Stroke(width = stroke),
        )
        drawArc(
            brush = Brush.horizontalGradient(listOf(PoseCyanBright, PoseGlowPurple)),
            startAngle = RING_START_ANGLE,
            sweepAngle = FULL_SWEEP * percent / PERCENT_SCALE,
            useCenter = false,
            topLeft = Offset(inset, inset),
            size = arcSize,
            style = Stroke(width = stroke, cap = StrokeCap.Round),
        )
    }
}

@Composable
private fun StartPosingButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(CtaHeight)
            .poseGradientPill(
                palette = GlossyBadgePalette.HeroCta,
                cornerRadius = CtaCorner,
            )
            .bounceClick(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        Image(
            painter = painterResource(R.drawable.ic_pose_camera_cta),
            contentDescription = null,
            modifier = Modifier.size(width = 20.dp, height = 14.dp),
        )
        Spacer(modifier = Modifier.width(21.dp))
        Text(
            text = stringResource(R.string.pose_detail_cta),
            style = poseTextStyle(16.sp, FontWeight.Bold, Color.White),
            maxLines = 1,
        )
    }
}
