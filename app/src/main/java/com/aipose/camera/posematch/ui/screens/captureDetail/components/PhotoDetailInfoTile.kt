package com.aipose.camera.posematch.ui.screens.captureDetail.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
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
import com.aipose.camera.posematch.ui.common.GlossyIconBadge
import com.aipose.camera.posematch.ui.screens.captureDetail.models.PhotoDetailInfo
import com.aipose.camera.posematch.ui.theme.LocalAppPalette
import com.aipose.camera.posematch.ui.theme.PoseChipBottom
import com.aipose.camera.posematch.ui.theme.PoseChipTop
import com.aipose.camera.posematch.ui.theme.poseTextStyle
import java.util.Locale

private val TileShape = RoundedCornerShape(18.dp)
private val TileMinHeight = 64.dp
private val BadgeSize = 28.dp
private const val TILE_BORDER_ALPHA = 0.09f

@Composable
internal fun PhotoDetailInfoTile(
    info: PhotoDetailInfo,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .heightIn(min = TileMinHeight)
            .clip(TileShape)
            .background(Brush.verticalGradient(listOf(PoseChipTop, PoseChipBottom)))
            .border(1.dp, Color.White.copy(alpha = TILE_BORDER_ALPHA), TileShape)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        GlossyIconBadge(
            iconRes = info.iconRes,
            palette = info.palette,
            size = BadgeSize,
            glyphSize = info.glyphSize,
        )
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = info.label.uppercase(Locale.getDefault()),
                style = poseTextStyle(8.5.sp, FontWeight.Bold, LocalAppPalette.current.textFaint),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = info.value,
                style = poseTextStyle(14.sp, FontWeight.Bold, Color.White),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}
