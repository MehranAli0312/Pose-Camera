package com.aipose.camera.posematch.ui.common

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.aipose.camera.posematch.ui.models.GlossyBadgePalette
import com.aipose.camera.posematch.ui.theme.LocalAppPalette
import com.aipose.camera.posematch.ui.theme.PoseTextBright
import com.aipose.camera.posematch.ui.theme.poseTextStyle

private val CardCorner = 28.dp
private val CardMaxWidth = 340.dp
private val BadgeSize = 58.dp
private val BadgeGlyphSize = 30.dp
private val ActionHeight = 48.dp
private val ActionCorner = 24.dp
private val ActionGap = 12.dp
private val TitleSize = 18.sp
private val MessageSize = 12.5.sp
private val ActionLabelSize = 14.sp
private const val SECONDARY_BORDER_ALPHA = 0.12f

@Composable
fun PoseDialog(
    @DrawableRes iconRes: Int,
    iconPalette: GlossyBadgePalette,
    title: String,
    message: String,
    confirmLabel: String,
    onConfirm: () -> Unit,
    dismissLabel: String,
    onDismiss: () -> Unit,
) {
    Dialog(onDismissRequest = onDismiss) {
        PoseDialogCard(
            iconRes = iconRes,
            iconPalette = iconPalette,
            title = title,
            message = message,
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(ActionGap),
            ) {
                PoseDialogAction(
                    label = dismissLabel,
                    labelColor = PoseTextBright,
                    onClick = onDismiss,
                    modifier = Modifier
                        .weight(1f)
                        .poseRaisedSurface(
                            shape = RoundedCornerShape(ActionCorner),
                            borderAlpha = SECONDARY_BORDER_ALPHA,
                        ),
                )
                PoseDialogAction(
                    label = confirmLabel,
                    labelColor = Color.White,
                    onClick = onConfirm,
                    modifier = Modifier
                        .weight(1f)
                        .poseGradientPill(
                            palette = iconPalette,
                            cornerRadius = ActionCorner,
                            glossHeight = 20.dp,
                        ),
                )
            }
        }
    }
}

@Composable
fun PoseDialogCard(
    @DrawableRes iconRes: Int,
    iconPalette: GlossyBadgePalette,
    title: String,
    message: String,
    modifier: Modifier = Modifier,
    actions: @Composable () -> Unit,
) {
    Column(
        modifier = modifier
            .widthIn(max = CardMaxWidth)
            .fillMaxWidth()
            .poseElevatedSurface(cornerRadius = CardCorner)
            .padding(horizontal = 24.dp, vertical = 26.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        GlossyIconBadge(
            iconRes = iconRes,
            palette = iconPalette,
            size = BadgeSize,
            glyphSize = BadgeGlyphSize,
        )
        Text(
            text = title,
            style = poseTextStyle(TitleSize, FontWeight.Bold, Color.White),
            textAlign = TextAlign.Center,
        )
        Text(
            text = message,
            style = poseTextStyle(MessageSize, FontWeight.Normal, LocalAppPalette.current.textMuted)
                .copy(lineHeight = 18.sp),
            textAlign = TextAlign.Center,
        )
        Box(modifier = Modifier.padding(top = 8.dp)) { actions() }
    }
}

@Composable
fun PoseDialogAction(
    label: String,
    labelColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .height(ActionHeight)
            .bounceClick(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            style = poseTextStyle(ActionLabelSize, FontWeight.Bold, labelColor),
            maxLines = 1,
        )
    }
}
