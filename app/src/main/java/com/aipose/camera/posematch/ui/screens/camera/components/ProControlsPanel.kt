package com.aipose.camera.posematch.ui.screens.camera.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aipose.camera.posematch.R
import com.aipose.camera.posematch.ui.common.bounceClick
import com.aipose.camera.posematch.ui.screens.camera.models.CaptureTimer
import com.aipose.camera.posematch.ui.theme.LocalAppPalette
import java.util.Locale
import kotlin.math.roundToInt

private const val ISO_MINIMUM = 100f
private const val ISO_MAXIMUM = 3200f
private const val EXPOSURE_MINIMUM = -2f
private const val EXPOSURE_MAXIMUM = 2f
private const val EXPOSURE_FORMAT = "%.1f"
private val ToggleShape = RoundedCornerShape(10.dp)
private val ToggleIdleColor = Color(0xFF1E1E24)

@Composable
internal fun ProControlsPanel(
    isGridVisible: Boolean,
    timer: CaptureTimer,
    iso: Int,
    exposure: Float,
    onToggleGrid: () -> Unit,
    onCycleTimer: () -> Unit,
    onIsoChange: (Int) -> Unit,
    onExposureChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = LocalAppPalette.current
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(palette.card)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = stringResource(R.string.camera_pro_title),
            color = Color.LightGray,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            ProToggle(
                icon = Icons.Default.GridOn,
                label = stringResource(R.string.camera_grid),
                isActive = isGridVisible,
                onClick = onToggleGrid,
                modifier = Modifier.weight(1f),
            )
            ProToggle(
                icon = Icons.Default.Timer,
                label = if (timer.isEnabled) {
                    stringResource(R.string.camera_timer_seconds, timer.seconds)
                } else {
                    stringResource(R.string.camera_timer)
                },
                isActive = timer.isEnabled,
                onClick = onCycleTimer,
                modifier = Modifier.weight(1f),
            )
        }

        ProSliderRow(
            label = stringResource(R.string.camera_iso),
            value = iso.toFloat(),
            valueRange = ISO_MINIMUM..ISO_MAXIMUM,
            valueLabel = iso.toString(),
            onValueChange = { value -> onIsoChange(value.roundToInt()) },
        )

        ProSliderRow(
            label = stringResource(R.string.camera_ev),
            value = exposure,
            valueRange = EXPOSURE_MINIMUM..EXPOSURE_MAXIMUM,
            valueLabel = String.format(Locale.US, EXPOSURE_FORMAT, exposure),
            onValueChange = onExposureChange,
        )
    }
}

@Composable
private fun ProToggle(
    icon: ImageVector,
    label: String,
    isActive: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val accent = LocalAppPalette.current.accent
    Row(
        modifier = modifier
            .clip(ToggleShape)
            .background(if (isActive) accent.copy(alpha = 0.22f) else ToggleIdleColor)
            .bounceClick(onClick = onClick)
            .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (isActive) accent else Color.Gray,
            modifier = Modifier.size(18.dp),
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = label,
            color = if (isActive) accent else Color.White,
            fontSize = 12.sp,
        )
    }
}

@Composable
private fun ProSliderRow(
    label: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    valueLabel: String,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
) {
    val accent = LocalAppPalette.current.accent
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            color = Color.Gray,
            modifier = Modifier.width(48.dp),
            fontSize = 11.sp,
        )
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            colors = SliderDefaults.colors(thumbColor = accent, activeTrackColor = accent),
            modifier = Modifier.weight(1f),
        )
        Text(
            text = valueLabel,
            color = Color.White,
            modifier = Modifier.width(36.dp),
            fontSize = 11.sp,
            textAlign = TextAlign.End,
        )
    }
}
