package com.aipose.camera.posematch.ui.screens.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aipose.camera.posematch.R
import com.aipose.camera.posematch.ui.theme.LocalAppPalette

@Composable
internal fun PoseSearchEmptyCard(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(140.dp)
            .background(LocalAppPalette.current.card, RoundedCornerShape(16.dp)),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = stringResource(R.string.home_no_blueprints),
            color = Color.Gray,
            fontSize = 14.sp,
        )
    }
}
