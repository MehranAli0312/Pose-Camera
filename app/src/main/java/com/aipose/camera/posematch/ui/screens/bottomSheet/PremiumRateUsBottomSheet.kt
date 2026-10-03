package com.aipose.camera.posematch.ui.screens.bottomSheet

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.dp
import com.aipose.camera.posematch.ui.common.PremiumRateUsContent
import com.aipose.camera.posematch.ui.common.PremiumRateUsMaxWidth

private val SheetSideMargin = 20.dp
private val SheetBottomMargin = 20.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PremiumRateUsBottomSheet(
    maxStars: Int = 5,
    rating: Float,
    openFullDialogCustom: MutableState<Boolean>,
    onRatingChanged: (Float) -> Unit,
    onRatingCallback: () -> Unit,
) {
    if (!openFullDialogCustom.value) return

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val onDismiss = { openFullDialogCustom.value = false }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RectangleShape,
        containerColor = Color.Transparent,
        contentColor = MaterialTheme.colorScheme.onBackground,
        dragHandle = null,
    ) {
        PremiumRateUsContent(
            rating = rating,
            maxStars = maxStars,
            onRatingChanged = onRatingChanged,
            onSubmit = onRatingCallback,
            onDismiss = onDismiss,
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = SheetSideMargin)
                .padding(bottom = SheetBottomMargin)
                .widthIn(max = PremiumRateUsMaxWidth),
        )
    }
}
