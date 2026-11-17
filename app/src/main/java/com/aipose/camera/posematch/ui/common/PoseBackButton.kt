package com.aipose.camera.posematch.ui.common

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import com.aipose.camera.posematch.R

private val BackButtonSize = 40.dp
private val BackGlyphSize = 32.dp

@Composable
fun PoseBackButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tint: Color = MaterialTheme.colorScheme.onBackground,
) {
    Box(
        modifier = modifier
            .size(BackButtonSize)
            .bounceClick(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_back),
            contentDescription = stringResource(R.string.action_back),
            tint = tint,
            modifier = Modifier.size(BackGlyphSize),
        )
    }
}
