package com.aipose.camera.posematch.ui.screens.settings.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aipose.camera.posematch.R
import com.aipose.camera.posematch.ui.common.GlossyIconBadge
import com.aipose.camera.posematch.ui.common.click
import com.aipose.camera.posematch.ui.models.GlossyBadgePalette
import com.aipose.camera.posematch.ui.theme.PoseRaisedBottom
import com.aipose.camera.posematch.ui.theme.PoseRaisedTop
import com.aipose.camera.posematch.ui.theme.PoseTextFaint
import com.aipose.camera.posematch.ui.theme.PoseVioletLight
import com.aipose.camera.posematch.ui.theme.poseTextStyle

private val RowMinHeight = 56.dp
private val RowShape = RoundedCornerShape(20.dp)
private val BadgeSize = 36.dp
private val RowStartPadding = 16.dp
private val RowEndPadding = 22.dp
private val BadgeToTitle = 14.dp
private val TitleToTrailing = 16.dp
private val ValueToChevron = 11.dp
private val ChevronWidth = 7.dp
private val ChevronHeight = 12.dp
private val TitleSize = 13.5.sp
private val ValueSize = 12.sp

private const val ROW_BORDER_ALPHA = 0.09f

@Composable
internal fun SettingRow(
    @DrawableRes iconRes: Int,
    palette: GlossyBadgePalette,
    title: String,
    modifier: Modifier = Modifier,
    value: String? = null,
    onClick: (() -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = RowMinHeight)
            .clip(RowShape)
            .background(Brush.verticalGradient(listOf(PoseRaisedTop, PoseRaisedBottom)))
            .border(1.dp, Color.White.copy(alpha = ROW_BORDER_ALPHA), RowShape)
            .then(if (onClick != null) Modifier.click(onClick = onClick) else Modifier)
            .padding(start = RowStartPadding, end = RowEndPadding, top = 8.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        GlossyIconBadge(
            iconRes = iconRes,
            palette = palette,
            size = BadgeSize,
        )
        Spacer(modifier = Modifier.width(BadgeToTitle))
        Text(
            text = title,
            style = poseTextStyle(TitleSize, FontWeight.Bold, Color.White),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
        if (trailing != null) {
            Spacer(modifier = Modifier.width(TitleToTrailing))
            trailing()
        } else {
            if (value != null) {
                Text(
                    text = value,
                    style = poseTextStyle(ValueSize, FontWeight.Bold, PoseVioletLight),
                )
                Spacer(modifier = Modifier.width(ValueToChevron))
            }
            Icon(
                painter = painterResource(R.drawable.ic_pose_chevron_end),
                contentDescription = null,
                tint = PoseTextFaint,
                modifier = Modifier.size(width = ChevronWidth, height = ChevronHeight),
            )
        }
    }
}
