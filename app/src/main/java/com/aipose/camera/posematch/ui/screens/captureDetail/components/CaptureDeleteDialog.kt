package com.aipose.camera.posematch.ui.screens.captureDetail.components

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aipose.camera.posematch.R
import com.aipose.camera.posematch.ui.theme.DangerRed

@Composable
internal fun CaptureDeleteDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        icon = {
            Icon(
                imageVector = Icons.Default.DeleteForever,
                contentDescription = null,
                tint = DangerRed,
            )
        },
        title = {
            Text(
                text = stringResource(R.string.delete_title),
                color = Color.White,
                fontWeight = FontWeight.Bold,
            )
        },
        text = {
            Text(
                text = stringResource(R.string.delete_message),
                color = Color.Gray,
                fontSize = 13.sp,
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = DangerRed),
                shape = RoundedCornerShape(12.dp),
            ) {
                Text(
                    text = stringResource(R.string.action_delete),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                )
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss, shape = RoundedCornerShape(12.dp)) {
                Text(text = stringResource(R.string.action_cancel), color = Color.White)
            }
        },
    )
}
