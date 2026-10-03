package com.aipose.camera.posematch.ui.common

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SectionTitle(
    title: String,
    modifier: Modifier = Modifier,
    trailing: String? = null,
    onTrailingClick: (() -> Unit)? = null,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 4.dp, end = 4.dp, top = 10.dp, bottom = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.titleMedium.copy(
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
            ),
        )
        if (trailing != null) {
            Text(
                text = trailing,
                modifier = if (onTrailingClick != null) Modifier.bounceClick(onClick = onTrailingClick) else Modifier,
                style = MaterialTheme.typography.labelLarge.copy(
                    fontSize = if (onTrailingClick != null) 12.5.sp else 12.sp,
                    fontWeight = if (onTrailingClick != null) FontWeight.Bold else FontWeight.SemiBold,
                    color = if (onTrailingClick != null) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                ),
            )
        }
    }
}
