package com.aipose.camera.posematch.ui.screens.collections.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.HistoryToggleOff
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aipose.camera.posematch.R

@Composable
internal fun CollectionsEmptyState(
    query: String,
    modifier: Modifier = Modifier,
) {
    val isSearching = query.isNotEmpty()
    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = if (isSearching) Icons.Default.SearchOff else Icons.Default.HistoryToggleOff,
                contentDescription = null,
                tint = Color.DarkGray,
                modifier = Modifier.size(64.dp),
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = if (isSearching) {
                    stringResource(R.string.history_no_match, query)
                } else {
                    stringResource(R.string.history_empty)
                },
                color = Color.Gray,
            )
            if (!isSearching) {
                Text(
                    text = stringResource(R.string.history_empty_sub),
                    color = Color.DarkGray,
                    fontSize = 11.sp,
                )
            }
        }
    }
}
