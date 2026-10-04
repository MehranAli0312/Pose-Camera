package com.aipose.camera.posematch.ui.screens.photoEdit.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aipose.camera.posematch.ui.common.bounceClick
import com.aipose.camera.posematch.ui.common.poseRaisedSurface
import com.aipose.camera.posematch.ui.screens.photoEdit.models.CropTransform
import com.aipose.camera.posematch.ui.theme.LocalAppPalette
import com.aipose.camera.posematch.ui.theme.PoseTextBright
import com.aipose.camera.posematch.ui.theme.poseTextStyle

private val ButtonSize = 56.dp
private val ButtonShape = RoundedCornerShape(20.dp)
private val GlyphSize = 20.dp
private const val BUTTON_BORDER_ALPHA = 0.1f

@Composable
internal fun CropTransformRow(
    onTransform: (CropTransform) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        CropTransform.entries.forEach { transform ->
            CropTransformButton(
                transform = transform,
                onClick = { onTransform(transform) },
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun CropTransformButton(
    transform: CropTransform,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val label = stringResource(transform.labelRes)
    Column(
        modifier = modifier
            .bounceClick(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(ButtonSize)
                .poseRaisedSurface(ButtonShape, borderAlpha = BUTTON_BORDER_ALPHA),
            contentAlignment = Alignment.Center,
        ) {
            Image(
                painter = painterResource(transform.iconRes),
                contentDescription = label,
                colorFilter = ColorFilter.tint(PoseTextBright),
                modifier = Modifier.size(GlyphSize),
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = label,
            style = poseTextStyle(8.5.sp, FontWeight.Bold, LocalAppPalette.current.textFaint),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
