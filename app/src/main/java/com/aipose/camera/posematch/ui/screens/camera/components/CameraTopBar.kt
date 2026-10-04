package com.aipose.camera.posematch.ui.screens.camera.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aipose.camera.posematch.R
import com.aipose.camera.posematch.ui.common.bounceClick
import com.aipose.camera.posematch.ui.screens.camera.models.FlashMode
import com.aipose.camera.posematch.ui.theme.PoseAmber
import com.aipose.camera.posematch.ui.theme.PoseAmberLight
import com.aipose.camera.posematch.ui.theme.poseTextStyle

private val BarHeight = 52.dp
private val ButtonSize = 36.dp
private val TitleHeight = 32.dp
private val TitleMaxWidth = 200.dp
private val TitleShape = RoundedCornerShape(16.dp)
private val GlyphSize = 16.dp
private val ChevronSize = 10.dp
private const val SCRIM_ALPHA = 0.45f
private const val BORDER_ALPHA = 0.12f

@Composable
internal fun CameraTopBar(
    poseTitle: String,
    flashMode: FlashMode,
    onBack: () -> Unit,
    onChangePose: () -> Unit,
    onToggleFlash: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .height(BarHeight)
            .padding(horizontal = 22.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        GlassCircleButton(
            iconRes = R.drawable.ic_back,
            contentDescription = stringResource(R.string.action_back),
            onClick = onBack,
        )
        PoseTitleChip(title = poseTitle, onClick = onChangePose)
        FlashButton(flashMode = flashMode, onClick = onToggleFlash)
    }
}

@Composable
private fun PoseTitleChip(
    title: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .widthIn(max = TitleMaxWidth)
            .height(TitleHeight)
            .clip(TitleShape)
            .background(Color.Black.copy(alpha = SCRIM_ALPHA))
            .border(1.dp, Color.White.copy(alpha = BORDER_ALPHA), TitleShape)
            .bounceClick(onClick = onClick)
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = title,
            style = poseTextStyle(12.sp, FontWeight.Bold, Color.White),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Image(
            painter = painterResource(R.drawable.ic_pose_chevron_down),
            contentDescription = stringResource(R.string.camera_change_pose),
            colorFilter = ColorFilter.tint(Color.White),
            modifier = Modifier.size(ChevronSize),
        )
    }
}

@Composable
private fun GlassCircleButton(
    iconRes: Int,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(ButtonSize)
            .clip(CircleShape)
            .background(Color.Black.copy(alpha = SCRIM_ALPHA))
            .border(1.dp, Color.White.copy(alpha = BORDER_ALPHA), CircleShape)
            .bounceClick(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Image(
            painter = painterResource(iconRes),
            contentDescription = contentDescription,
            colorFilter = ColorFilter.tint(Color.White),
            modifier = Modifier.size(GlyphSize),
        )
    }
}

@Composable
private fun FlashButton(
    flashMode: FlashMode,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scrim = Color.Black.copy(alpha = SCRIM_ALPHA)
    val background = if (flashMode == FlashMode.Off) {
        Brush.verticalGradient(listOf(scrim, scrim))
    } else {
        Brush.verticalGradient(listOf(PoseAmberLight, PoseAmber))
    }
    Box(
        modifier = modifier
            .size(ButtonSize)
            .clip(CircleShape)
            .background(background)
            .border(1.dp, Color.White.copy(alpha = BORDER_ALPHA), CircleShape)
            .bounceClick(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Image(
            painter = painterResource(
                if (flashMode == FlashMode.Off) {
                    R.drawable.ic_camera_flash_off
                } else {
                    R.drawable.ic_camera_flash
                }
            ),
            contentDescription = stringResource(R.string.camera_flash),
            colorFilter = ColorFilter.tint(Color.White),
            modifier = Modifier.size(GlyphSize),
        )
        if (flashMode == FlashMode.Auto) {
            Text(
                text = stringResource(R.string.camera_flash_auto),
                style = poseTextStyle(7.sp, FontWeight.Bold, Color.White),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 3.dp, bottom = 2.dp),
            )
        }
    }
}
