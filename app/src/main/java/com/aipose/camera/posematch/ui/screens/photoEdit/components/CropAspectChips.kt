package com.aipose.camera.posematch.ui.screens.photoEdit.components

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aipose.camera.posematch.ui.common.bounceClick
import com.aipose.camera.posematch.ui.screens.photoEdit.models.CropAspect
import com.aipose.camera.posematch.ui.theme.LocalAppPalette

@Composable
internal fun CropAspectChips(
    selectedRatio: Float?,
    onAspectSelected: (Float?) -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = LocalAppPalette.current
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CropAspect.entries.forEach { aspect ->
            val isSelected = selectedRatio == aspect.ratio
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (isSelected) palette.accent else palette.card)
                    .bounceClick(onClick = { onAspectSelected(aspect.ratio) })
                    .padding(horizontal = 14.dp, vertical = 9.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = stringResource(aspect.labelRes),
                    color = if (isSelected) Color.White else Color.Gray,
                    fontSize = 12.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                )
            }
        }
    }
}
