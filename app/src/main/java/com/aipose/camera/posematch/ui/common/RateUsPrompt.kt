package com.aipose.camera.posematch.ui.common

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import com.aipose.camera.posematch.ui.vm.RateUsViewModel
import kotlinx.coroutines.delay
import org.koin.compose.koinInject

private const val PROMPT_DELAY_MILLIS = 900L

@Composable
fun RateUsPrompt(
    savedShotCount: Int,
    matchScore: Int,
    viewModel: RateUsViewModel = koinInject(),
) {
    val sheetVisible = rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(savedShotCount, matchScore) {
        if (!viewModel.shouldPrompt(savedShotCount, matchScore)) return@LaunchedEffect
        delay(PROMPT_DELAY_MILLIS)
        viewModel.markPrompted()
        sheetVisible.value = true
    }

    RateUsSheet(visible = sheetVisible, viewModel = viewModel)
}
