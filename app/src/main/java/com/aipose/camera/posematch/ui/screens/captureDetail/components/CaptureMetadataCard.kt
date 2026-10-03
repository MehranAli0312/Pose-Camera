package com.aipose.camera.posematch.ui.screens.captureDetail.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.aipose.camera.posematch.R
import com.aipose.camera.posematch.ui.models.matchScoreColor
import com.aipose.camera.posematch.ui.screens.collections.models.CaptureUi
import com.aipose.camera.posematch.ui.theme.LocalAppPalette

@Composable
internal fun CaptureMetadataCard(
    captureUi: CaptureUi,
    onShare: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(LocalAppPalette.current.card)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        CaptureDetailRow(
            icon = Icons.Default.LocationOn,
            label = stringResource(R.string.detail_location),
            value = captureUi.locationLabel,
        )
        CaptureDetailRow(
            icon = Icons.Default.Schedule,
            label = stringResource(R.string.detail_captured),
            value = captureUi.formattedDate,
        )
        CaptureDetailRow(
            icon = Icons.Default.Analytics,
            label = stringResource(R.string.detail_match_score),
            value = stringResource(R.string.score_percent, captureUi.capture.matchScore),
            valueColor = matchScoreColor(captureUi.capture.matchScore),
        )
        CaptureDetailRow(
            icon = Icons.Default.Category,
            label = stringResource(R.string.detail_category),
            value = captureUi.capture.category,
        )
        Spacer(modifier = Modifier.height(4.dp))
        CaptureDetailActions(onShare = onShare, onDelete = onDelete)
    }
}
