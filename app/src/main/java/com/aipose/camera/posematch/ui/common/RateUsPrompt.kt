package com.aipose.camera.posematch.ui.common

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import com.example.common.showToast
import com.aipose.camera.posematch.R
import com.aipose.camera.posematch.domain.models.RateUsOutcome
import com.aipose.camera.posematch.ui.screens.bottomSheet.PremiumRateUsBottomSheet
import com.aipose.camera.posematch.ui.vm.RateUsViewModel
import com.aipose.camera.posematch.util.contact
import com.aipose.camera.posematch.util.getAppLink
import kotlinx.coroutines.delay
import org.koin.compose.koinInject

private const val PromptDelayMs = 900L
private const val DefaultRating = 5f

@Composable
fun RateUsPrompt(
    cleanedItems: Int,
    freedBytes: Long,
    viewModel: RateUsViewModel = koinInject(),
) {
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current
    val sheetVisible = rememberSaveable { mutableStateOf(false) }
    var rating by rememberSaveable { mutableFloatStateOf(DefaultRating) }

    LaunchedEffect(cleanedItems, freedBytes) {
        if (!viewModel.shouldPrompt(cleanedItems, freedBytes)) return@LaunchedEffect
        delay(PromptDelayMs)
        viewModel.markPrompted()
        sheetVisible.value = true
    }

    PremiumRateUsBottomSheet(
        rating = rating,
        openFullDialogCustom = sheetVisible,
        onRatingChanged = { rating = it },
        onRatingCallback = {
            when (viewModel.submitRating(rating)) {
                RateUsOutcome.OpenStore -> {
                    val appLink = context.getAppLink()
                    sheetVisible.value = false
                    if (appLink.isNotEmpty()) {
                        uriHandler.openUri(appLink)
                    }
                }

                RateUsOutcome.SendFeedback -> {
                    sheetVisible.value = false
                    context.contact()
                }

                RateUsOutcome.NotRated -> {
                    context.showToast(context.getString(R.string.please_rate))
                }
            }
        },
    )
}
