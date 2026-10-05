package com.aipose.camera.posematch.ui.screens.progress.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aipose.camera.posematch.R
import com.aipose.camera.posematch.domain.models.CategoryScore
import com.aipose.camera.posematch.ui.common.poseElevatedSurface
import com.aipose.camera.posematch.ui.screens.progress.models.categoryAccentColor
import com.aipose.camera.posematch.ui.theme.LocalAppPalette
import com.aipose.camera.posematch.ui.theme.PoseTextBright
import com.aipose.camera.posematch.ui.theme.poseTextStyle
import com.aipose.camera.posematch.util.bidiIsolate

private val CardCorner = 26.dp
private val BarHeight = 6.dp
private val BarShape = RoundedCornerShape(3.dp)

private const val MAX_SCORE = 100f
private const val TRACK_ALPHA = 0.1f

@Composable
internal fun ProgressCategoriesCard(
    categories: List<CategoryScore>,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .poseElevatedSurface(CardCorner)
            .padding(start = 16.dp, end = 16.dp, top = 20.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = stringResource(R.string.progress_categories_title),
            style = poseTextStyle(13.5.sp, FontWeight.Bold, Color.White),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        if (categories.isEmpty()) {
            Text(
                text = stringResource(R.string.progress_categories_empty),
                style = poseTextStyle(11.sp, FontWeight.Normal, LocalAppPalette.current.textMuted),
                modifier = Modifier.padding(vertical = 8.dp),
            )
        }
        categories.forEach { category ->
            CategoryRow(category = category)
        }
    }
}

@Composable
private fun CategoryRow(category: CategoryScore) {
    val accent = categoryAccentColor(category.category)
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = category.category,
                style = poseTextStyle(11.5.sp, FontWeight.Bold, PoseTextBright),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = stringResource(R.string.score_percent, category.averageMatch).bidiIsolate(),
                style = poseTextStyle(11.sp, FontWeight.Bold, accent),
                maxLines = 1,
            )
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(BarHeight)
                .clip(BarShape)
                .background(Color.White.copy(alpha = TRACK_ALPHA)),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth((category.averageMatch / MAX_SCORE).coerceIn(0f, 1f))
                    .clip(BarShape)
                    .background(accent),
            )
        }
    }
}
