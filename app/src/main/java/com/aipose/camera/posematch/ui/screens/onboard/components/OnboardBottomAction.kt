package com.aipose.camera.posematch.ui.screens.onboard.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.aipose.camera.posematch.ui.common.AppButton

private val ActionHeight = 56.dp

@Composable
internal fun OnboardBottomAction(
    isLastPage: Boolean,
    buttonText: String,
    onFinish: () -> Unit,
    onNext: () -> Unit,
) {
    AppButton(
        text = buttonText,
        onClick = {
            if (isLastPage) onFinish() else onNext()
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(ActionHeight),
        verticalPadding = 0.dp,
        textStyle = MaterialTheme.typography.titleMedium,
    )
}
