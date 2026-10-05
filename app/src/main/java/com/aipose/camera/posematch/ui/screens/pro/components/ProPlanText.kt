package com.aipose.camera.posematch.ui.screens.pro.components

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import com.aipose.camera.posematch.R
import com.aipose.camera.posematch.domain.models.PremiumPlan
import com.aipose.camera.posematch.domain.models.ProPlan
import com.aipose.camera.posematch.ui.screens.pro.models.ProEvent
import com.aipose.camera.posematch.ui.screens.pro.models.ProPlansState
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
internal fun proPlanSubtitle(details: PremiumPlan?): String {
    if (details == null) return stringResource(R.string.pro_plan_unavailable)
    val price = details.formattedPrice.bidiIsolate()
    return when {
        details.hasTrial -> pluralStringResource(
            R.plurals.pro_trial_then_price,
            details.trialDays,
            details.trialDays,
            price,
        )

        details.plan == ProPlan.YEARLY -> stringResource(R.string.pro_billed_yearly, price)
        else -> stringResource(R.string.pro_billed_monthly)
    }
}

@Composable
internal fun rememberMonthlyPrice(details: PremiumPlan): String? = remember(details) {
    val price = when (details.plan.billingMonths) {
        1 -> details.formattedPrice
        else -> formatLikePlayPrice(details.formattedPrice, details.monthlyPriceMicros)
    }
    price?.bidiIsolate()
}

@Composable
internal fun proCtaText(state: ProPlansState): String = stringResource(
    when {
        state is ProPlansState.Unavailable -> R.string.pro_retry
        state is ProPlansState.Content && state.startsWithTrial -> R.string.pro_try_for_free
        else -> R.string.pro_upgrade_title
    },
)

internal fun Context.proEventMessage(event: ProEvent): String = when (event) {
    ProEvent.Purchased -> getString(R.string.pro_purchase_success)
    ProEvent.AlreadyOwned -> getString(R.string.pro_purchase_already_owned)
    ProEvent.PurchasePending -> getString(R.string.pro_purchase_pending)
    ProEvent.PurchaseCancelled -> getString(R.string.pro_purchase_cancelled)
    is ProEvent.PurchaseFailed -> getString(R.string.pro_purchase_failed, event.reason)
    ProEvent.Restored -> getString(R.string.pro_restore_success)
    ProEvent.NothingToRestore -> getString(R.string.pro_restore_nothing_found)
    ProEvent.RestoreFailed -> getString(R.string.pro_restore_failed)
}

private fun formatLikePlayPrice(playPrice: String, micros: Long): String? {
    if (micros <= 0) return null
    val amountRange = PlayPriceAmount.find(playPrice)?.range ?: return null
    val amount = NumberFormat.getNumberInstance().apply {
        maximumFractionDigits = MAX_PRICE_FRACTION_DIGITS
    }.format(micros / MICROS_PER_UNIT)
    return playPrice.replaceRange(amountRange, amount)
}
