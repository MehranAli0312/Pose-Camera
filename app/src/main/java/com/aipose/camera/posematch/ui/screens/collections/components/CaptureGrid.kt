package com.aipose.camera.posematch.ui.screens.collections.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.aipose.camera.posematch.ui.screens.collections.models.CaptureUi

internal const val CAPTURE_ROW_SIZE = 3

@Composable
internal fun CaptureGrid(
    captures: List<CaptureUi>,
    onCaptureClick: (CaptureUi) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        captures.chunked(CAPTURE_ROW_SIZE).forEach { rowCaptures ->
            CaptureRow(captures = rowCaptures, onCaptureClick = onCaptureClick)
        }
    }
}

@Composable
internal fun CaptureRow(
    captures: List<CaptureUi>,
    onCaptureClick: (CaptureUi) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        captures.forEach { captureUi ->
            CaptureThumbnail(
                captureUi = captureUi,
                onClick = { onCaptureClick(captureUi) },
                modifier = Modifier.weight(1f),
            )
        }
        repeat(CAPTURE_ROW_SIZE - captures.size) { Spacer(modifier = Modifier.weight(1f)) }
    }
}
