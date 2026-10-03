package com.aipose.camera.posematch.ui.screens.camera.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.FlashAuto
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.aipose.camera.posematch.R
import com.aipose.camera.posematch.ui.screens.camera.models.FlashMode
import com.aipose.camera.posematch.ui.theme.LocalAppPalette

@Composable
internal fun CameraTopBar(
    flashMode: FlashMode,
    areControlsHighlighted: Boolean,
    onBack: () -> Unit,
    onToggleFlash: () -> Unit,
    onToggleControls: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val accent = LocalAppPalette.current.accent
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.Black)
            .statusBarsPadding()
            .height(44.dp)
            .padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onBack) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = stringResource(R.string.action_back),
                tint = Color.White,
            )
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onToggleFlash) {
                Icon(
                    imageVector = when (flashMode) {
                        FlashMode.On -> Icons.Default.FlashOn
                        FlashMode.Auto -> Icons.Default.FlashAuto
                        FlashMode.Off -> Icons.Default.FlashOff
                    },
                    contentDescription = stringResource(R.string.camera_flash),
                    tint = if (flashMode == FlashMode.Off) Color.White else accent,
                )
            }
            IconButton(onClick = onToggleControls) {
                Icon(
                    imageVector = Icons.Default.Tune,
                    contentDescription = stringResource(R.string.camera_controls),
                    tint = if (areControlsHighlighted) accent else Color.White,
                )
            }
        }
    }
}
