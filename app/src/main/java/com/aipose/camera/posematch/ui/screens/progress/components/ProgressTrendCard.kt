package com.aipose.camera.posematch.ui.screens.progress.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aipose.camera.posematch.R
import com.aipose.camera.posematch.domain.models.ProgressPeriod
import com.aipose.camera.posematch.domain.models.TrendBucket
import com.aipose.camera.posematch.ui.common.LtrLayout
import com.aipose.camera.posematch.ui.common.poseElevatedSurface
import com.aipose.camera.posematch.ui.screens.progress.models.trendRangeRes
import com.aipose.camera.posematch.ui.theme.Indigo
import com.aipose.camera.posematch.ui.theme.LocalAppPalette
import com.aipose.camera.posematch.ui.theme.PoseIndigoLight
import com.aipose.camera.posematch.ui.theme.PoseTrendBarBottom
import com.aipose.camera.posematch.ui.theme.PoseTrendBarTop
import com.aipose.camera.posematch.ui.theme.PoseVioletBright
import com.aipose.camera.posematch.ui.theme.poseTextStyle
import com.aipose.camera.posematch.util.bidiIsolate
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val CardCorner = 26.dp
private val CardMinHeight = 190.dp
private val ChartHeight = 100.dp
private val BarWidth = 26.dp
private val BarShape = RoundedCornerShape(9.dp)
private val BarMinHeight = 4.dp
private val DashLength = 4.dp

private const val MAX_SCORE = 100f
private const val AVERAGE_LINE_ALPHA = 0.16f
private const val WEEKDAY_PATTERN = "EEEEE"
private const val MONTH_PATTERN = "LLL"

@Composable
internal fun ProgressTrendCard(
    period: ProgressPeriod,
    buckets: List<TrendBucket>,
    averageMatch: Int,
    modifier: Modifier = Modifier,
) {
    val faint = LocalAppPalette.current.textFaint
    val labels = rememberBucketLabels(period, buckets)
    Column(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = CardMinHeight)
            .poseElevatedSurface(CardCorner)
            .padding(start = 16.dp, end = 16.dp, top = 18.dp, bottom = 14.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = stringResource(R.string.progress_trend_title),
                style = poseTextStyle(13.5.sp, FontWeight.Bold, Color.White),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = stringResource(period.trendRangeRes),
                style = poseTextStyle(9.5.sp, FontWeight.Bold, faint),
                maxLines = 1,
            )
        }
        Spacer(modifier = Modifier.height(14.dp))
        LtrLayout {
            Column {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(ChartHeight)
                        .drawBehind { drawAverageLine(averageMatch / MAX_SCORE) },
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(ChartHeight),
                        verticalAlignment = Alignment.Bottom,
                    ) {
                        buckets.forEach { bucket ->
                            Box(
                                modifier = Modifier.weight(1f),
                                contentAlignment = Alignment.BottomCenter,
                            ) {
                                TrendBar(
                                    value = bucket.averageMatch,
                                    isHighlighted = bucket.isAbove(averageMatch),
                                )
                            }
                        }
                    }
                    Text(
                        text = stringResource(
                            R.string.progress_trend_average,
                            stringResource(R.string.score_percent, averageMatch).bidiIsolate(),
                        ),
                        style = poseTextStyle(8.sp, FontWeight.Bold, faint),
                        maxLines = 1,
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth()) {
                    buckets.forEachIndexed { index, bucket ->
                        Text(
                            text = labels.getOrElse(index) { "" },
                            style = poseTextStyle(
                                9.5.sp,
                                FontWeight.Bold,
                                if (bucket.isAbove(averageMatch)) PoseIndigoLight else faint,
                            ),
                            textAlign = TextAlign.Center,
                            maxLines = 1,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TrendBar(
    value: Int,
    isHighlighted: Boolean,
) {
    val brush = if (isHighlighted) {
        Brush.verticalGradient(listOf(PoseVioletBright, Indigo))
    } else {
        Brush.verticalGradient(listOf(PoseTrendBarTop, PoseTrendBarBottom))
    }
    Box(
        modifier = Modifier
            .width(BarWidth)
            .height((ChartHeight * (value / MAX_SCORE)).coerceIn(BarMinHeight, ChartHeight))
            .background(brush, BarShape),
    )
}

@Composable
private fun rememberBucketLabels(period: ProgressPeriod, buckets: List<TrendBucket>): List<String> {
    val weekLabels = buckets.indices.map { index ->
        stringResource(R.string.progress_trend_week_label, index + 1)
    }
    return remember(period, buckets, weekLabels) {
        when (period) {
            ProgressPeriod.Week -> buckets.formatted(WEEKDAY_PATTERN)
            ProgressPeriod.Month -> weekLabels
            ProgressPeriod.AllTime -> buckets.formatted(MONTH_PATTERN)
        }
    }
}

private fun List<TrendBucket>.formatted(pattern: String): List<String> {
    val format = SimpleDateFormat(pattern, Locale.getDefault())
    return map { bucket -> format.format(Date(bucket.startMillis)) }
}

private fun TrendBucket.isAbove(averageMatch: Int): Boolean =
    shotCount > 0 && this.averageMatch > averageMatch

private fun DrawScope.drawAverageLine(fraction: Float) {
    if (fraction <= 0f) return
    val y = size.height * (1f - fraction.coerceIn(0f, 1f))
    val dash = DashLength.toPx()
    drawLine(
        color = Color.White.copy(alpha = AVERAGE_LINE_ALPHA),
        start = Offset(0f, y),
        end = Offset(size.width, y),
        strokeWidth = 1.dp.toPx(),
        pathEffect = PathEffect.dashPathEffect(floatArrayOf(dash, dash)),
    )
}
