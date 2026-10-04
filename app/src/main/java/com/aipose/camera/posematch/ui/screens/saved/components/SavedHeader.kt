package com.aipose.camera.posematch.ui.screens.saved.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aipose.camera.posematch.R
import com.aipose.camera.posematch.ui.common.bounceClick
import com.aipose.camera.posematch.ui.theme.LocalAppPalette
import com.aipose.camera.posematch.ui.theme.PoseFilterGlyph
import com.aipose.camera.posematch.ui.theme.PoseRaisedBottom
import com.aipose.camera.posematch.ui.theme.PoseRaisedTop
import com.aipose.camera.posematch.ui.theme.PoseShadow
import com.aipose.camera.posematch.ui.theme.poseTextStyle

private val FilterButtonSize = 40.dp
private val FilterGlyphWidth = 18.dp
private val FilterGlyphHeight = 17.dp
private val ShadowOffsetY = 4.dp

private const val BUTTON_BORDER_ALPHA = 0.14f
private const val BUTTON_SHADOW_ALPHA = 0.5f

@Composable
internal fun SavedHeader(
    totalCount: Int,
    onOpenSort: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.nav_saved),
                style = poseTextStyle(26.sp, FontWeight.Bold, Color.White),
                modifier = Modifier.weight(1f),
            )
            SortButton(onClick = onOpenSort)
        }
        Spacer(modifier = Modifier.height(11.dp))
        Text(
            text = pluralStringResource(R.plurals.saved_subtitle, totalCount, totalCount),
            style = poseTextStyle(12.sp, FontWeight.Normal, LocalAppPalette.current.textMuted),
        )
    }
}

@Composable
private fun SortButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(FilterButtonSize)
            .drawBehind {
                drawCircle(
                    color = PoseShadow.copy(alpha = BUTTON_SHADOW_ALPHA),
                    radius = size.minDimension / 2f,
                    center = center.copy(y = center.y + ShadowOffsetY.toPx()),
                )
            }
            .clip(CircleShape)
            .background(Brush.verticalGradient(listOf(PoseRaisedTop, PoseRaisedBottom)))
            .border(1.dp, Color.White.copy(alpha = BUTTON_BORDER_ALPHA), CircleShape)
            .bounceClick(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Image(
            painter = painterResource(R.drawable.ic_pose_filter),
            contentDescription = stringResource(R.string.saved_sort_title),
            colorFilter = ColorFilter.tint(PoseFilterGlyph),
            modifier = Modifier.size(width = FilterGlyphWidth, height = FilterGlyphHeight),
        )
    }
}
