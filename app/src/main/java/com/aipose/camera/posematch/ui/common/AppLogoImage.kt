package com.aipose.camera.posematch.ui.common

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import com.aipose.camera.posematch.R
import com.aipose.camera.posematch.ui.theme.LocalAppPalette

private const val LOGO_BITMAP_SIZE = 216
private val LogoShape = RoundedCornerShape(28.dp)

@Composable
fun AppLogoImage(
    size: Dp,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val logoBitmap = remember(context) {
        ContextCompat.getDrawable(context, R.mipmap.ic_launcher)
            ?.toBitmap(LOGO_BITMAP_SIZE, LOGO_BITMAP_SIZE)
            ?.asImageBitmap()
    } ?: return

    Image(
        bitmap = logoBitmap,
        contentDescription = stringResource(R.string.splash_logo_content_description),
        modifier = modifier
            .size(size)
            .clip(LogoShape)
            .border(1.5.dp, LocalAppPalette.current.accent, LogoShape),
        contentScale = ContentScale.Crop,
    )
}
