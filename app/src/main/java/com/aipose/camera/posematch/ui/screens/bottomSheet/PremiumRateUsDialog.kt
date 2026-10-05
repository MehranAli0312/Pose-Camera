package com.aipose.camera.posematch.ui.screens.bottomSheet

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.aipose.camera.posematch.ui.common.ImmersiveDialogWindowEffect
import com.aipose.camera.posematch.ui.common.PremiumRateUsContent
import com.aipose.camera.posematch.ui.common.PremiumRateUsMaxWidth

private val DialogSideMargin = 20.dp
private val DialogGlowMargin = 24.dp

@Composable
fun PremiumRateUsDialog(
    maxStars: Int = 5,
    rating: Float,
    openFullDialogCustom: MutableState<Boolean>,
    onRatingChanged: (Float) -> Unit,
    onRatingCallback: () -> Unit,
) {
    if (!openFullDialogCustom.value) return

    val onDismiss = { openFullDialogCustom.value = false }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        ImmersiveDialogWindowEffect()
        PremiumRateUsContent(
            rating = rating,
            maxStars = maxStars,
            onRatingChanged = onRatingChanged,
            onSubmit = onRatingCallback,
            onDismiss = onDismiss,
            modifier = Modifier
                .padding(horizontal = DialogSideMargin, vertical = DialogGlowMargin)
                .widthIn(max = PremiumRateUsMaxWidth)
                .fillMaxWidth(),
        )
    }
}
