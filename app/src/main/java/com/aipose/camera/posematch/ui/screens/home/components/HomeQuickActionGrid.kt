package com.aipose.camera.posematch.ui.screens.home.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aipose.camera.posematch.ui.common.GlossyIconBadge
import com.aipose.camera.posematch.ui.common.bounceClick
import com.aipose.camera.posematch.ui.common.poseCard
import com.aipose.camera.posematch.ui.screens.home.models.HomeActionSubtitle
import com.aipose.camera.posematch.ui.screens.home.models.HomeQuickAction
import com.aipose.camera.posematch.ui.theme.LocalAppPalette
import com.aipose.camera.posematch.ui.theme.poseTextStyle

private const val TILES_PER_ROW = 4
private val TileShape = RoundedCornerShape(22.dp)
private val TileHeight = 102.dp
private val TileGap = 10.dp
private val RowGap = 10.dp
private val BadgeSize = 44.dp

@Composable
internal fun HomeQuickActionGrid(
    actions: List<HomeQuickAction>,
    onActionClick: (HomeQuickAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(RowGap),
    ) {
        actions.chunked(TILES_PER_ROW).forEach { rowActions ->
            Row(horizontalArrangement = Arrangement.spacedBy(TileGap)) {
                rowActions.forEach { action ->
                    QuickActionTile(action = action, onClick = { onActionClick(action) })
                }
            }
        }
    }
}

@Composable
private fun RowScope.QuickActionTile(
    action: HomeQuickAction,
    onClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .weight(1f)
            .height(TileHeight)
            .poseCard(TileShape)
            .bounceClick(onClick = onClick)
            .padding(horizontal = 6.dp, vertical = 14.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        GlossyIconBadge(
            iconRes = action.id.iconRes,
            palette = action.id.palette,
            size = BadgeSize,
        )
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = stringResource(action.id.titleRes),
            style = poseTextStyle(11.5.sp, FontWeight.Bold, Color.White),
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = action.subtitleText(),
            style = poseTextStyle(8.5.sp, FontWeight.Normal, LocalAppPalette.current.textFaint),
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun HomeQuickAction.subtitleText(): String = when (val subtitle = id.subtitle) {
    is HomeActionSubtitle.Label -> stringResource(subtitle.textRes)
    is HomeActionSubtitle.Count -> pluralStringResource(subtitle.pluralRes, count, count)
}
