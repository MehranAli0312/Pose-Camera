package com.aipose.camera.posematch.ui.screens.collections.components

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.aipose.camera.posematch.R

@Composable
internal fun CollectionsHeader(
    totalCount: Int,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Text(
            text = stringResource(R.string.history_title),
            style = MaterialTheme.typography.headlineSmall.copy(
                color = Color.White,
                fontWeight = FontWeight.Bold,
            ),
        )
        Text(
            text = stringResource(R.string.history_subtitle, totalCount),
            style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray),
        )
    }
}
