package com.aipose.camera.posematch.ui.common

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp

@Composable
fun PlaceHolder(src: Int) {
    Image(
        painter = painterResource(src), contentDescription = null
    )
}

@Composable
fun BoxCard(
    showProg: Boolean = true,
    modifier: Modifier = Modifier,
    content: (@Composable () -> Unit),
) {
    Box(
        modifier = modifier.background(MaterialTheme.colorScheme.secondaryContainer),
        contentAlignment = Alignment.Center,
    ) {
        content()

        if (showProg) CircularProgressIndicator(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(10.dp)
                .size(18.dp),
            strokeWidth = 2.dp,
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.9f),
        )
    }
}
