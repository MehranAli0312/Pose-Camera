package com.aipose.camera.posematch.ui.common

import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.aipose.camera.posematch.R

@Composable
fun PoseBackButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tint: Color = MaterialTheme.colorScheme.onBackground,
) {
    Icon(
        painter = painterResource(R.drawable.ic_back),
        contentDescription = stringResource(R.string.action_back),
        tint = tint,
        modifier = modifier.bounceClick(onClick = onClick),
    )
}
