package com.aipose.camera.posematch.ui.screens.captureDetail.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aipose.camera.posematch.R
import com.aipose.camera.posematch.ui.theme.DangerRed
import com.aipose.camera.posematch.ui.theme.LocalAppPalette

@Composable
internal fun CaptureDetailActions(
    onShare: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = modifier.fillMaxWidth(),
    ) {
        Button(
            onClick = onShare,
            colors = ButtonDefaults.buttonColors(
                containerColor = LocalAppPalette.current.accent
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.weight(1f),
        ) {
            Icon(
                imageVector = Icons.Default.Share,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(18.dp),
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = stringResource(R.string.action_share),
                color = Color.White,
                fontSize = 13.sp,
            )
        }
        OutlinedButton(
            onClick = onDelete,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.weight(1f),
        ) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = null,
                tint = DangerRed,
                modifier = Modifier.size(18.dp),
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = stringResource(R.string.action_delete),
                color = DangerRed,
                fontSize = 13.sp,
            )
        }
    }
}
