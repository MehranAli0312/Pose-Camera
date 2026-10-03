package com.aipose.camera.posematch.ui.screens.photoEdit.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aipose.camera.posematch.R
import com.aipose.camera.posematch.ui.theme.LocalAppPalette

@Composable
internal fun PhotoEditTopBar(
    isSaving: Boolean,
    onDiscard: () -> Unit,
    onSave: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        IconButton(onClick = onDiscard) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = stringResource(R.string.action_discard),
                tint = Color.White,
            )
        }
        Text(
            text = stringResource(R.string.title_edit),
            color = Color.White,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
        )
        Button(
            onClick = onSave,
            enabled = !isSaving,
            colors = ButtonDefaults.buttonColors(containerColor = LocalAppPalette.current.accent),
            shape = RoundedCornerShape(10.dp),
            contentPadding = PaddingValues(horizontal = 18.dp, vertical = 4.dp),
            modifier = Modifier.height(38.dp),
        ) {
            Text(
                text = stringResource(R.string.action_save),
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}
