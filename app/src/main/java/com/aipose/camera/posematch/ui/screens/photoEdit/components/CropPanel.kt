package com.aipose.camera.posematch.ui.screens.photoEdit.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.aipose.camera.posematch.R
import com.aipose.camera.posematch.domain.models.PhotoGeometry
import com.aipose.camera.posematch.ui.screens.photoEdit.models.CropAspect
import com.aipose.camera.posematch.ui.screens.photoEdit.models.CropTransform
import com.aipose.camera.posematch.ui.theme.PoseVioletLight
import com.aipose.camera.posematch.util.bidiIsolate
import kotlin.math.roundToInt

private val StraightenRange = PhotoGeometry.MIN_STRAIGHTEN..PhotoGeometry.MAX_STRAIGHTEN

@Composable
internal fun CropPanel(
    selectedAspect: CropAspect,
    straightenDegrees: Float,
    onAspectSelected: (CropAspect) -> Unit,
    onTransform: (CropTransform) -> Unit,
    onStraightenChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        CropAspectChips(
            selected = selectedAspect,
            onAspectSelected = onAspectSelected,
        )
        Spacer(modifier = Modifier.height(16.dp))
        CropTransformRow(onTransform = onTransform)
        Spacer(modifier = Modifier.height(12.dp))
        EditSliderCard(
            label = stringResource(R.string.edit_straighten),
            value = stringResource(R.string.edit_degrees, straightenDegrees.roundToInt()).bidiIsolate(),
            valueColor = PoseVioletLight,
            cornerRadius = 20.dp,
            verticalPadding = 12.dp,
            modifier = Modifier.padding(horizontal = 20.dp),
        ) {
            StraightenRuler(
                degrees = straightenDegrees,
                range = StraightenRange,
                onDegreesChange = onStraightenChange,
            )
        }
    }
}
