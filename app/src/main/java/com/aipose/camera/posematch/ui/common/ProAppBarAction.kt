package com.aipose.camera.posematch.ui.common

import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.aipose.camera.posematch.R

@Composable
fun ProAppBarAction(onClick: () -> Unit) {
    if (isProUser()) return

    LottieView(
        animationRes = R.raw.pro_anim,
        modifier = Modifier
            .size(52.dp)
            .bounceClick(onClick = onClick),
    )
}
