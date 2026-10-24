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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aipose.camera.posematch.ui.common.GlossyIconBadge
import com.aipose.camera.posematch.ui.common.bounceClick
import com.aipose.camera.posematch.ui.screens.home.models.HomeQuickActionId
import com.aipose.camera.posematch.ui.theme.poseTextStyle

private const val TILES_PER_ROW = 4
private val TileShape = RoundedCornerShape(12.dp)
private val TileMinHeight = 136.dp
private val TileGap = 8.dp
private val RowGap = 5.dp
private val BadgeSize = 40.dp

@Composable
internal fun HomeQuickActionGrid(
    actions: List<HomeQuickActionId>,
    onActionClick: (HomeQuickActionId) -> Unit,
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
    action: HomeQuickActionId,
    onClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .weight(1f)
            .bounceClick(onClick = onClick)
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        GlossyIconBadge(
            iconRes = action.iconRes,
            palette = action.palette,
            size = BadgeSize,
        )
        Spacer(modifier = Modifier.height(9.dp))
        Text(
            text = stringResource(action.titleRes),
            style = poseTextStyle(13.sp, FontWeight.Bold, Color.White),
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
