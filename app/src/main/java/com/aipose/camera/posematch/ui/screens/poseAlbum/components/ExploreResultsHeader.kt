package com.aipose.camera.posematch.ui.screens.poseAlbum.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aipose.camera.posematch.R
import com.aipose.camera.posematch.ui.common.bounceClick
import com.aipose.camera.posematch.ui.theme.PoseVioletLight
import com.aipose.camera.posematch.ui.theme.poseTextStyle

private val SortGlyphWidth = 12.dp
private val SortGlyphHeight = 11.dp

@Composable
internal fun ExploreResultsHeader(
    count: Int,
    onOpenSort: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = pluralStringResource(R.plurals.explore_showing, count, count),
            style = poseTextStyle(13.sp, FontWeight.Bold, Color.White),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
        Row(
            modifier = Modifier
                .bounceClick(onClick = onOpenSort)
                .padding(start = 12.dp, top = 4.dp, bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = stringResource(R.string.explore_sort),
                style = poseTextStyle(11.sp, FontWeight.Bold, PoseVioletLight),
                maxLines = 1,
            )
            Image(
                painter = painterResource(R.drawable.ic_pose_filter),
                contentDescription = stringResource(R.string.explore_sort_title),
                colorFilter = ColorFilter.tint(PoseVioletLight),
                modifier = Modifier.size(width = SortGlyphWidth, height = SortGlyphHeight),
            )
        }
    }
}
