package com.aipose.camera.posematch.ui.screens.home.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Icon
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
import com.aipose.camera.posematch.R
import com.aipose.camera.posematch.ui.common.bounceClick
import com.aipose.camera.posematch.ui.screens.home.models.iconForCategory
import com.aipose.camera.posematch.ui.theme.LocalAppPalette

@Composable
internal fun PoseCategoryHeader(
    category: String,
    showAll: Boolean,
    onShowAll: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val accent = LocalAppPalette.current.accent
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = iconForCategory(category),
                contentDescription = null,
                tint = accent,
                modifier = Modifier.size(16.dp),
            )
            Spacer(modifier = Modifier.width(7.dp))
            Text(
                text = category,
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
            )
        }
        if (showAll) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .bounceClick(onClick = onShowAll)
                    .padding(horizontal = 6.dp, vertical = 4.dp),
            ) {
                Text(
                    text = stringResource(R.string.see_all),
                    color = accent,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                )
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = accent,
                    modifier = Modifier.size(14.dp),
                )
            }
        }
    }
}
