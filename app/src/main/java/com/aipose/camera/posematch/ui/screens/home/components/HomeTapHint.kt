package com.aipose.camera.posematch.ui.screens.home.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aipose.camera.posematch.R

@Composable
internal fun HomeTapHint(modifier: Modifier = Modifier) {
    Text(
        text = stringResource(R.string.home_tap_hint),
        color = Color.Gray,
        fontSize = 12.sp,
        modifier = modifier.padding(top = 2.dp),
    )
}
