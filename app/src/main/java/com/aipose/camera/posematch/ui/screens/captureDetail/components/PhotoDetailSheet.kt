package com.aipose.camera.posematch.ui.screens.captureDetail.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import com.aipose.camera.posematch.ui.common.safeBottomSystemBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aipose.camera.posematch.ui.screens.captureDetail.models.PhotoDetailInfo
import com.aipose.camera.posematch.ui.theme.LocalAppPalette
import com.aipose.camera.posematch.ui.theme.PoseSheetBottom
import com.aipose.camera.posematch.ui.theme.PoseSheetTop
import com.aipose.camera.posematch.ui.theme.poseTextStyle

private val SheetShape = RoundedCornerShape(topStart = 34.dp, topEnd = 34.dp)
private val HandleWidth = 56.dp
private val HandleHeight = 5.dp
private val GlossInset = 12.dp
private val GlossTop = 6.dp
private val GlossHeight = 54.dp
private const val SHEET_BORDER_ALPHA = 0.12f
private const val HANDLE_ALPHA = 0.28f
private const val GLOSS_ALPHA = 0.42f
private const val GLOSS_LAYER_ALPHA = 0.12f
private const val INFO_COLUMNS = 2

@Composable
internal fun PhotoDetailSheet(
    title: String,
    subtitle: String,
    infos: List<PhotoDetailInfo>,
    modifier: Modifier = Modifier,
    actions: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(SheetShape)
            .background(Brush.verticalGradient(listOf(PoseSheetTop, PoseSheetBottom)))
            .drawBehind {
                val inset = GlossInset.toPx()
                val top = GlossTop.toPx()
                val height = GlossHeight.toPx()
                drawRoundRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color.White.copy(alpha = GLOSS_ALPHA), Color.Transparent),
                        startY = top,
                        endY = top + height,
                    ),
                    topLeft = Offset(inset, top),
                    size = Size(size.width - inset * 2f, height),
                    cornerRadius = CornerRadius(height / 2f),
                    alpha = GLOSS_LAYER_ALPHA,
                )
            }
            .border(1.dp, Color.White.copy(alpha = SHEET_BORDER_ALPHA), SheetShape)
            .safeBottomSystemBarsPadding()
            .padding(start = 24.dp, end = 24.dp, top = 18.dp, bottom = 28.dp),
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .width(HandleWidth)
                .height(HandleHeight)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = HANDLE_ALPHA))
        )
        Spacer(modifier = Modifier.height(17.dp))
        Text(
            text = title,
            style = poseTextStyle(22.sp, FontWeight.Bold, Color.White),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = subtitle,
            style = poseTextStyle(11.sp, FontWeight.Normal, LocalAppPalette.current.textMuted),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Spacer(modifier = Modifier.height(18.dp))
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            infos.chunked(INFO_COLUMNS).forEach { rowInfos ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    rowInfos.forEach { info ->
                        PhotoDetailInfoTile(info = info, modifier = Modifier.weight(1f))
                    }
                    repeat(INFO_COLUMNS - rowInfos.size) { Spacer(modifier = Modifier.weight(1f)) }
                }
            }
        }
        Spacer(modifier = Modifier.height(18.dp))
        actions()
    }
}
