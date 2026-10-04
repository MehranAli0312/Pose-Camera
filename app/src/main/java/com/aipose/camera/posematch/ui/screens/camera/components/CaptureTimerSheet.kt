package com.aipose.camera.posematch.ui.screens.camera.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.aipose.camera.posematch.ui.common.GlossyIconBadge
import com.aipose.camera.posematch.ui.common.bounceClick
import com.aipose.camera.posematch.ui.common.poseGradientPill
import com.aipose.camera.posematch.ui.common.poseRaisedSurface
import com.aipose.camera.posematch.ui.models.GlossyBadgePalette
import com.aipose.camera.posematch.ui.screens.camera.models.CaptureTimer
import com.aipose.camera.posematch.ui.theme.LocalAppPalette
import com.aipose.camera.posematch.ui.theme.PoseAmber
import com.aipose.camera.posematch.ui.theme.PoseAmberLight
import com.aipose.camera.posematch.ui.theme.PoseSheetRaisedBottom
import com.aipose.camera.posematch.ui.theme.PoseSheetRaisedTop
import com.aipose.camera.posematch.ui.theme.PoseTextBright
import com.aipose.camera.posematch.ui.theme.poseTextStyle
import com.aipose.camera.posematch.util.bidiIsolate

private val SheetShape = RoundedCornerShape(topStart = 34.dp, topEnd = 34.dp)
private val HandleWidth = 56.dp
private val HandleHeight = 5.dp
private val CloseSize = 36.dp
private val CloseGlyphSize = 14.dp
private val OptionHeight = 96.dp
private val OptionCorner = 24.dp
private val OptionShape = RoundedCornerShape(OptionCorner)
private val OptionGlyphSize = 20.dp
private val HintShape = RoundedCornerShape(18.dp)
private val HintHeight = 48.dp
private val HintBadgeSize = 24.dp
private val HintGlyphSize = 14.dp
private val ApplyHeight = 52.dp
private val ApplyCorner = 26.dp
private const val SHEET_BORDER_ALPHA = 0.12f
private const val HANDLE_ALPHA = 0.28f
private const val CLOSE_FILL_ALPHA = 0.07f
private const val CLOSE_BORDER_ALPHA = 0.12f
private const val OPTION_BORDER_ALPHA = 0.1f
private const val HINT_FILL_ALPHA = 0.12f
private const val HINT_BORDER_ALPHA = 0.3f

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun CaptureTimerSheet(
    selected: CaptureTimer,
    onApply: (CaptureTimer) -> Unit,
    onDismiss: () -> Unit,
) {
    var draft by remember(selected) { mutableStateOf(selected) }
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        contentWindowInsets = { WindowInsets(0) },
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = Color.Transparent,
        shape = SheetShape,
        tonalElevation = 0.dp,
        dragHandle = null,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(SheetShape)
                .background(Brush.verticalGradient(listOf(PoseSheetRaisedTop, PoseSheetRaisedBottom)))
                .border(1.dp, Color.White.copy(alpha = SHEET_BORDER_ALPHA), SheetShape)
                .navigationBarsPadding()
                .padding(start = 28.dp, end = 28.dp, top = 18.dp, bottom = 20.dp),
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .width(HandleWidth)
                    .height(HandleHeight)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = HANDLE_ALPHA))
            )
            Spacer(modifier = Modifier.height(18.dp))
            TimerSheetHeader(onClose = onDismiss)
            Spacer(modifier = Modifier.height(24.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                CaptureTimer.entries.forEach { timer ->
                    TimerOption(
                        timer = timer,
                        isSelected = timer == draft,
                        onClick = { draft = timer },
                        modifier = Modifier.weight(1f),
                    )
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
            TimerHint(timer = draft)
            Spacer(modifier = Modifier.height(22.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(ApplyHeight)
                    .poseGradientPill(
                        palette = GlossyBadgePalette.HeroCta,
                        cornerRadius = ApplyCorner,
                        glossHeight = 23.dp,
                    )
                    .bounceClick(onClick = { onApply(draft) }),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = stringResource(R.string.timer_sheet_apply),
                    style = poseTextStyle(15.sp, FontWeight.Bold, Color.White),
                )
            }
        }
    }
}

@Composable
private fun TimerSheetHeader(
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top,
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = stringResource(R.string.settings_capture_timer),
                style = poseTextStyle(20.sp, FontWeight.Bold, Color.White),
            )
            Text(
                text = stringResource(R.string.timer_sheet_subtitle),
                style = poseTextStyle(12.sp, FontWeight.Normal, LocalAppPalette.current.textMuted),
            )
        }
        Box(
            modifier = Modifier
                .size(CloseSize)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = CLOSE_FILL_ALPHA))
                .border(1.dp, Color.White.copy(alpha = CLOSE_BORDER_ALPHA), CircleShape)
                .bounceClick(onClick = onClose),
            contentAlignment = Alignment.Center,
        ) {
            Image(
                painter = painterResource(R.drawable.ic_close),
                contentDescription = stringResource(R.string.action_cancel),
                colorFilter = ColorFilter.tint(PoseTextBright),
                modifier = Modifier.size(CloseGlyphSize),
            )
        }
    }
}

@Composable
private fun TimerOption(
    timer: CaptureTimer,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val mutedColor = LocalAppPalette.current.textMuted
    val contentColor = if (isSelected) Color.White else mutedColor
    val surface = if (isSelected) {
        Modifier.poseGradientPill(
            palette = GlossyBadgePalette.Violet,
            cornerRadius = OptionCorner,
            glossInsetX = 6.dp,
            glossTop = 4.dp,
            glossHeight = 34.dp,
        )
    } else {
        Modifier.poseRaisedSurface(OptionShape, borderAlpha = OPTION_BORDER_ALPHA)
    }
    Column(
        modifier = modifier
            .heightIn(min = OptionHeight)
            .then(surface)
            .bounceClick(onClick = onClick)
            .padding(vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Image(
            painter = painterResource(timer.iconRes),
            contentDescription = null,
            colorFilter = ColorFilter.tint(contentColor),
            modifier = Modifier.size(OptionGlyphSize),
        )
        Spacer(modifier = Modifier.height(18.dp))
        Text(
            text = timerLabel(timer),
            style = poseTextStyle(12.sp, FontWeight.Bold, contentColor),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun TimerHint(
    timer: CaptureTimer,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = HintHeight)
            .clip(HintShape)
            .background(PoseAmber.copy(alpha = HINT_FILL_ALPHA))
            .border(1.dp, PoseAmber.copy(alpha = HINT_BORDER_ALPHA), HintShape)
            .padding(horizontal = 15.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(11.dp),
    ) {
        GlossyIconBadge(
            iconRes = R.drawable.ic_camera_timer,
            palette = GlossyBadgePalette.Amber,
            size = HintBadgeSize,
            glyphSize = HintGlyphSize,
        )
        Text(
            text = if (timer.isEnabled) {
                stringResource(R.string.timer_sheet_hint, timer.seconds)
            } else {
                stringResource(R.string.timer_sheet_hint_off)
            },
            style = poseTextStyle(11.sp, FontWeight.Bold, PoseAmberLight),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun timerLabel(timer: CaptureTimer): String =
    if (timer.isEnabled) {
        stringResource(R.string.settings_capture_timer_seconds, timer.seconds).bidiIsolate()
    } else {
        stringResource(R.string.settings_capture_timer_off)
    }
