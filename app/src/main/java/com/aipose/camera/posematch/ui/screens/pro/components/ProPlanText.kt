package com.aipose.camera.posematch.ui.screens.pro.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import com.pdfutility.billing.presentation.states.PurchaseResult
import com.aipose.camera.posematch.R
import com.aipose.camera.posematch.ads.ProRestoreResult
import com.aipose.camera.posematch.domain.models.ProPlan
import com.aipose.camera.posematch.ui.screens.pro.models.ProPlanOption
import com.aipose.camera.posematch.ui.vm.ProUiState
import com.aipose.camera.posematch.util.bidiIsolate
import java.text.NumberFormat

private const val MICROS_PER_UNIT = 1_000_000.0
private const val MAX_PRICE_FRACTION_DIGITS = 2
private val PlayPriceAmount = Regex("""\d(?:[\d.,'\s  ]*\d)?""")

@Composable
internal fun proPlanTitle(plan: ProPlan): String = stringResource(
    when (plan) {
        ProPlan.YEARLY -> R.string.pro_plan_yearly
        ProPlan.MONTHLY -> R.string.pro_plan_monthly
    },
)

@Composable
internal fun proPlanSubtitle(option: ProPlanOption): String {
    val price = option.price?.bidiIsolate() ?: return stringResource(R.string.pro_plan_unavailable)
    val trialDays = option.freeTrialDays
    return when {
        trialDays != null -> pluralStringResource(
            R.plurals.pro_trial_then_price,
            trialDays,
            trialDays,
            price,
        )

        option.plan == ProPlan.YEARLY -> stringResource(R.string.pro_billed_yearly, price)
        else -> stringResource(R.string.pro_billed_monthly)
    }
}

@Composable
internal fun rememberMonthlyPrice(option: ProPlanOption): String? = remember(option) {
    val price = when (option.plan.billingMonths) {
        1 -> option.price
        else -> formatLikePlayPrice(option.price, option.monthlyPriceMicros)
    }
    price?.bidiIsolate()
}

@Composable
internal fun proCtaText(state: ProUiState): String = stringResource(
    when {
        state.isStoreUnavailable -> R.string.pro_retry
        state.selectedOption.freeTrialDays != null -> R.string.pro_try_for_free
        else -> R.string.pro_upgrade_title
    },
)

@Composable
internal fun proPurchaseMessage(result: PurchaseResult): String = when (result) {
    is PurchaseResult.Success -> stringResource(R.string.pro_purchase_success)
    PurchaseResult.AlreadyOwned -> stringResource(R.string.pro_purchase_already_owned)
    PurchaseResult.Pending -> stringResource(R.string.pro_purchase_pending)
    PurchaseResult.Cancelled -> stringResource(R.string.pro_purchase_cancelled)
    is PurchaseResult.Error -> stringResource(R.string.pro_purchase_failed, result.message)
}

@Composable
internal fun proRestoreMessage(result: ProRestoreResult): String = when (result) {
    ProRestoreResult.Restored -> stringResource(R.string.pro_restore_success)
    ProRestoreResult.NothingFound -> stringResource(R.string.pro_restore_nothing_found)
    ProRestoreResult.Failed -> stringResource(R.string.pro_restore_failed)
}

private fun formatLikePlayPrice(playPrice: String?, micros: Long?): String? {
    if (playPrice == null || micros == null) return null
    val amountRange = PlayPriceAmount.find(playPrice)?.range ?: return null
    val amount = NumberFormat.getNumberInstance().apply {
        maximumFractionDigits = MAX_PRICE_FRACTION_DIGITS
    }.format(micros / MICROS_PER_UNIT)
    return playPrice.replaceRange(amountRange, amount)
}
