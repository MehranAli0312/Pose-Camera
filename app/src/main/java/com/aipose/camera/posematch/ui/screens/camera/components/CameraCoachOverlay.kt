package com.aipose.camera.posematch.ui.screens.camera.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Accessibility
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Collections
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aipose.camera.posematch.R
import com.aipose.camera.posematch.ui.common.click
import com.aipose.camera.posematch.ui.theme.LocalAppPalette

@Composable
internal fun CameraCoachOverlay(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = LocalAppPalette.current
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.78f))
            .click { },
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier
                .padding(28.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(MaterialTheme.colorScheme.surface)
                .padding(22.dp)
        ) {
            Text(
                text = stringResource(R.string.coach_title),
                color = Color.White,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.height(18.dp))
            CoachStepRow(
                icon = Icons.Default.Collections,
                title = stringResource(R.string.coach_pick_title),
                description = stringResource(R.string.coach_pick_description),
            )
            Spacer(modifier = Modifier.height(14.dp))
            CoachStepRow(
                icon = Icons.Default.Accessibility,
                title = stringResource(R.string.coach_match_title),
                description = stringResource(R.string.coach_match_description),
            )
            Spacer(modifier = Modifier.height(14.dp))
            CoachStepRow(
                icon = Icons.Default.CameraAlt,
                title = stringResource(R.string.coach_snap_title),
                description = stringResource(R.string.coach_snap_description),
            )
            Spacer(modifier = Modifier.height(22.dp))
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = palette.accent),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = stringResource(R.string.coach_got_it),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}

@Composable
private fun CoachStepRow(
    icon: ImageVector,
    title: String,
    description: String,
    modifier: Modifier = Modifier,
) {
    val accent = LocalAppPalette.current.accent
    androidx.compose.foundation.layout.Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier,
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(accent.copy(alpha = 0.18f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = accent,
                modifier = Modifier.size(20.dp),
            )
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = description,
                color = Color.Gray,
                fontSize = 12.sp,
                lineHeight = 15.sp,
            )
        }
    }
}
