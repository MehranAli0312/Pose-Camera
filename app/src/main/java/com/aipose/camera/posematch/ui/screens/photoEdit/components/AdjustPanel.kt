package com.aipose.camera.posematch.ui.screens.photoEdit.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aipose.camera.posematch.R
import com.aipose.camera.posematch.ui.common.PoseGradientSlider
import com.aipose.camera.posematch.ui.common.click
import com.aipose.camera.posematch.ui.screens.photoEdit.models.AdjustTool
import com.aipose.camera.posematch.ui.theme.LocalAppPalette
import com.aipose.camera.posematch.ui.theme.PoseIndigo400
import com.aipose.camera.posematch.ui.theme.PoseVioletBright
import com.aipose.camera.posematch.ui.theme.PoseVioletLight
import com.aipose.camera.posematch.ui.theme.poseTextStyle
import com.aipose.camera.posematch.util.bidiIsolate
import java.util.Locale

private const val ADJUST_MINIMUM = -1f
private const val ADJUST_MAXIMUM = 1f
private const val SIGNED_VALUE_FORMAT = "%+.1f"
private val AdjustSliderColors = listOf(PoseIndigo400, PoseVioletBright)

@Composable
internal fun AdjustPanel(
    activeTool: AdjustTool,
    value: Float,
    isToolTouched: (AdjustTool) -> Boolean,
    onToolSelected: (AdjustTool) -> Unit,
    onValueChange: (Float) -> Unit,
    onResetAdjustments: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val toolLabel = stringResource(activeTool.labelRes)
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        AdjustToolRail(
            activeTool = activeTool,
            isToolTouched = isToolTouched,
            onToolSelected = onToolSelected,
        )
        Spacer(modifier = Modifier.height(10.dp))
        EditSliderCard(
            label = toolLabel.uppercase(Locale.getDefault()),
            value = stringResource(
                R.string.edit_signed_value,
                SIGNED_VALUE_FORMAT.format(Locale.getDefault(), value).bidiIsolate(),
            ),
            valueColor = PoseVioletLight,
            cornerRadius = 22.dp,
            verticalPadding = 16.dp,
            modifier = Modifier.padding(horizontal = 20.dp),
        ) {
            PoseGradientSlider(
                value = value,
                onValueChange = onValueChange,
                valueRange = ADJUST_MINIMUM..ADJUST_MAXIMUM,
                isCentered = true,
                activeColors = AdjustSliderColors,
                modifier = Modifier.fillMaxWidth(),
            )
        }
        Text(
            text = stringResource(R.string.edit_reset_adjustments),
            style = poseTextStyle(11.sp, FontWeight.Bold, LocalAppPalette.current.textMuted),
            modifier = Modifier
                .click(onClick = onResetAdjustments)
                .padding(horizontal = 16.dp, vertical = 10.dp),
        )
    }
}
