package com.aipose.camera.posematch.ui.screens.photoEdit.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aipose.camera.posematch.R
import com.aipose.camera.posematch.domain.models.PhotoFilterId
import com.aipose.camera.posematch.ui.common.PoseGradientSlider
import com.aipose.camera.posematch.util.bidiIsolate
import kotlin.math.roundToInt

private const val PERCENT_FACTOR = 100

@Composable
internal fun FiltersPanel(
    selectedFilter: PhotoFilterId,
    intensity: Float,
    onFilterSelected: (PhotoFilterId) -> Unit,
    onIntensityChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        PhotoFilterStrip(
            selectedFilter = selectedFilter,
            onFilterSelected = onFilterSelected,
        )
        Spacer(modifier = Modifier.height(22.dp))
        EditSliderCard(
            label = stringResource(R.string.edit_intensity),
            value = stringResource(R.string.score_percent, (intensity * PERCENT_FACTOR).roundToInt()).bidiIsolate(),
            valueColor = Color.White,
            valueSize = 11.sp,
            cornerRadius = 18.dp,
            hasGloss = false,
            verticalPadding = 11.dp,
            modifier = Modifier.padding(horizontal = 20.dp),
        ) {
            PoseGradientSlider(
                value = intensity,
                onValueChange = onIntensityChange,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}
