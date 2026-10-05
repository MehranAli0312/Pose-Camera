package com.aipose.camera.posematch.ui.screens.camera.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aipose.camera.posematch.R
import com.aipose.camera.posematch.ui.common.bounceClick
import com.aipose.camera.posematch.ui.screens.camera.models.FlashMode
import com.aipose.camera.posematch.ui.theme.PoseAmber400
import com.aipose.camera.posematch.ui.theme.PoseCardLabel
import com.aipose.camera.posematch.ui.theme.poseTextStyle

private val ButtonSize = 40.dp
private val TitleHeight = 32.dp
private val TitleMinWidth = 120.dp
private val TitleMaxWidth = 200.dp
private val TitleShape = RoundedCornerShape(16.dp)
private val BackGlyphSize = DpSize(10.dp, 17.dp)
private val FlashGlyphSize = DpSize(18.dp, 18.dp)
private val ChevronSize = DpSize(10.dp, 6.dp)
private const val TITLE_BORDER_ALPHA = 0.18f
private const val FLASH_BORDER_ALPHA = 0.5f

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
            .padding(start = 20.dp, end = 20.dp, top = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(ButtonSize)
                .cameraGlass(CircleShape)
                .bounceClick(onClick = onBack),
            contentAlignment = Alignment.Center,
        ) {
            Image(
                painter = painterResource(R.drawable.ic_pose_back),
                contentDescription = stringResource(R.string.action_back),
                colorFilter = ColorFilter.tint(Color.White),
                modifier = Modifier.size(BackGlyphSize),
            )
        }
        Box(
            modifier = Modifier.weight(1f),
            contentAlignment = Alignment.Center,
        ) {
            PoseTitleChip(title = poseTitle, onClick = onChangePose)
        }
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
            .widthIn(min = TitleMinWidth, max = TitleMaxWidth)
            .height(TitleHeight)
            .cameraGlass(
                shape = TitleShape,
                borderColor = Color.White.copy(alpha = TITLE_BORDER_ALPHA),
            )
            .bounceClick(onClick = onClick)
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
    ) {
        Text(
            text = title,
            style = poseTextStyle(12.sp, FontWeight.Bold, Color.White),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f, fill = false),
        )
        Image(
            painter = painterResource(R.drawable.ic_pose_chevron_down),
            contentDescription = stringResource(R.string.camera_change_pose),
            colorFilter = ColorFilter.tint(PoseCardLabel),
            modifier = Modifier.size(ChevronSize),
        )
    }
}

@Composable
private fun FlashButton(
    flashMode: FlashMode,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isOn = flashMode != FlashMode.Off
    Box(
        modifier = modifier
            .size(ButtonSize)
            .cameraGlass(
                shape = CircleShape,
                borderColor = if (isOn) {
                    PoseAmber400.copy(alpha = FLASH_BORDER_ALPHA)
                } else {
                    Color.White.copy(alpha = CAMERA_GLASS_BORDER_ALPHA)
                },
            )
            .bounceClick(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Image(
            painter = painterResource(if (isOn) R.drawable.ic_camera_flash else R.drawable.ic_camera_flash_off),
            contentDescription = stringResource(R.string.camera_flash),
            colorFilter = ColorFilter.tint(if (isOn) PoseAmber400 else Color.White),
            modifier = Modifier.size(FlashGlyphSize),
        )
        if (flashMode == FlashMode.Auto) {
            Text(
                text = stringResource(R.string.camera_flash_auto),
                style = poseTextStyle(7.sp, FontWeight.Bold, PoseAmber400),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 7.dp, bottom = 5.dp),
            )
        }
    }
}
