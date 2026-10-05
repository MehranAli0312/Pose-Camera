package com.aipose.camera.posematch.ui.common

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import com.aipose.camera.posematch.R
import com.aipose.camera.posematch.domain.models.RateUsOutcome
import com.aipose.camera.posematch.ui.screens.bottomSheet.PremiumRateUsBottomSheet
import com.aipose.camera.posematch.ui.vm.RateUsViewModel
import com.aipose.camera.posematch.util.contact
import com.aipose.camera.posematch.util.getAppLink
import com.example.common.showToast
import org.koin.compose.koinInject

private const val DEFAULT_RATING = 5f

@Composable
fun RateUsSheet(
    visible: MutableState<Boolean>,
    viewModel: RateUsViewModel = koinInject(),
) {
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current
    var rating by rememberSaveable { mutableFloatStateOf(DEFAULT_RATING) }

    PremiumRateUsBottomSheet(
        rating = rating,
        openFullDialogCustom = visible,
        onRatingChanged = { value -> rating = value },
        onRatingCallback = {
            when (viewModel.submitRating(rating)) {
                RateUsOutcome.OpenStore -> {
                    val appLink = context.getAppLink()
                    visible.value = false
                    if (appLink.isNotEmpty()) uriHandler.openUri(appLink)
                }

                RateUsOutcome.SendFeedback -> {
                    visible.value = false
                    context.contact()
                }

                RateUsOutcome.NotRated ->
                    context.showToast(context.getString(R.string.please_rate))
            }
        },
    )
}
