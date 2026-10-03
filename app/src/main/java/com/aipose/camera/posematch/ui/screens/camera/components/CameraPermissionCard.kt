package com.aipose.camera.posematch.ui.screens.camera.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aipose.camera.posematch.R
import com.aipose.camera.posematch.ui.theme.LocalAppPalette

@Composable
internal fun CameraPermissionCard(
    onAllow: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = LocalAppPalette.current
    Box(
        modifier = modifier.padding(28.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(palette.card)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Icon(
                imageVector = Icons.Default.PhotoCamera,
                contentDescription = null,
                tint = palette.accent,
                modifier = Modifier.size(44.dp),
            )
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = stringResource(R.string.perm_camera_title),
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = stringResource(R.string.perm_camera_desc),
                color = Color.Gray,
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(18.dp))
            Button(
                onClick = onAllow,
                colors = ButtonDefaults.buttonColors(containerColor = palette.accent),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = stringResource(R.string.perm_allow),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}
