package com.aipose.camera.posematch.ui.screens.achievements.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.aipose.camera.posematch.domain.models.BadgeStatus

private val WideLayoutMinWidth = 480.dp
private const val COMPACT_COLUMNS = 3
private const val WIDE_COLUMNS = 4

@Composable
internal fun AchievementBadgeGrid(
    badges: List<BadgeStatus>,
    modifier: Modifier = Modifier,
) {
    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        val columns = if (maxWidth >= WideLayoutMinWidth) WIDE_COLUMNS else COMPACT_COLUMNS
        Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
            badges.chunked(columns).forEach { row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    row.forEach { status ->
                        AchievementBadgeTile(
                            status = status,
                            modifier = Modifier.weight(1f),
                        )
                    }
                    repeat(columns - row.size) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}
