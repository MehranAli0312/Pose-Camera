package com.aipose.camera.posematch.ui.screens.photoEdit.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aipose.camera.posematch.R
import com.aipose.camera.posematch.domain.models.PhotoFilterId
import com.aipose.camera.posematch.ui.common.bounceClick
import com.aipose.camera.posematch.ui.screens.photoEdit.models.PhotoFilterStyle
import com.aipose.camera.posematch.ui.screens.photoEdit.models.photoFilterStyle
import com.aipose.camera.posematch.ui.theme.LocalAppPalette
import com.aipose.camera.posematch.ui.theme.Violet
import com.aipose.camera.posematch.ui.theme.poseTextStyle

private val SlotSize = 66.dp
private val TileSize = 60.dp
private val TileShape = RoundedCornerShape(18.dp)
private val RingShape = RoundedCornerShape(21.dp)
private val RingWidth = 2.5.dp
private val GlyphSize = 20.dp
private val BadgeSize = 18.dp
private val BadgeGlyphSize = 9.dp
private val GlossInset = 4.dp
private val GlossTop = 3.dp
private val GlossHeight = 24.dp
private const val GLOSS_ALPHA = 0.4f

@Composable
internal fun PhotoFilterStrip(
    selectedFilter: PhotoFilterId,
    onFilterSelected: (PhotoFilterId) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 17.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        items(PhotoFilterId.entries, key = { filterId -> filterId.key }) { filterId ->
            PhotoFilterTile(
                style = photoFilterStyle(filterId),
                isSelected = filterId == selectedFilter,
                onClick = { onFilterSelected(filterId) },
            )
        }
    }
}

@Composable
private fun PhotoFilterTile(
    style: PhotoFilterStyle,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val label = stringResource(style.labelRes)
    val mutedColor = LocalAppPalette.current.textMuted
    Column(
        modifier = modifier
            .width(SlotSize)
            .bounceClick(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier.size(SlotSize),
            contentAlignment = Alignment.Center,
        ) {
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .size(SlotSize)
                        .border(RingWidth, Violet, RingShape)
                )
            }
            Box(
                modifier = Modifier
                    .size(TileSize)
                    .clip(TileShape)
                    .background(Brush.linearGradient(listOf(style.top, style.bottom)))
                    .drawBehind { drawTileGloss() },
                contentAlignment = Alignment.Center,
            ) {
                Image(
                    painter = painterResource(style.iconRes),
                    contentDescription = label,
                    colorFilter = ColorFilter.tint(Color.White),
                    modifier = Modifier.size(GlyphSize),
                )
            }
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(BadgeSize)
                        .clip(CircleShape)
                        .background(Violet),
                    contentAlignment = Alignment.Center,
                ) {
                    Image(
                        painter = painterResource(R.drawable.ic_pose_check_small),
                        contentDescription = null,
                        colorFilter = ColorFilter.tint(Color.White),
                        modifier = Modifier.size(BadgeGlyphSize),
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            style = poseTextStyle(
                9.5.sp,
                if (isSelected) FontWeight.Bold else FontWeight.Normal,
                if (isSelected) Color.White else mutedColor,
            ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

private fun DrawScope.drawTileGloss() {
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
    )
}
