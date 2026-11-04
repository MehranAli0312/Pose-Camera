package com.aipose.camera.posematch.ui.screens.pro.components

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import com.aipose.camera.posematch.R
import com.aipose.camera.posematch.domain.models.PremiumPlan
import com.aipose.camera.posematch.domain.models.ProPlan
import com.aipose.camera.posematch.ui.screens.pro.models.ProEvent
import com.aipose.camera.posematch.ui.screens.pro.models.ProPlanOption
import com.aipose.camera.posematch.ui.screens.pro.models.ProPlansState
import com.aipose.camera.posematch.util.bidiIsolate

@Composable
internal fun proPlanTitle(plan: ProPlan): String = stringResource(
    when (plan) {
        ProPlan.YEARLY -> R.string.pro_plan_yearly
        ProPlan.WEEKLY -> R.string.pro_plan_weekly
        ProPlan.LIFETIME -> R.string.pro_plan_lifetime
    },
)

@Composable
internal fun proPlanPrice(details: PremiumPlan): String = details.formattedPrice.bidiIsolate()

@Composable
internal fun proPlanSubtitle(plan: ProPlan, details: PremiumPlan?): String {
    if (details == null) return stringResource(R.string.pro_plan_unavailable)
    if (details.hasTrial) {
        return pluralStringResource(
            R.plurals.pro_trial_then_price,
            details.trialDays,
            details.trialDays,
            proRecurringPrice(details),
        )
    }
    return stringResource(
        when (plan) {
            ProPlan.YEARLY -> R.string.pro_plan_yearly_note
            ProPlan.WEEKLY -> R.string.pro_plan_weekly_note
            ProPlan.LIFETIME -> R.string.pro_plan_lifetime_note
        },
    )
}

@Composable
private fun proRecurringPrice(details: PremiumPlan): String {
    val price = details.formattedPrice.bidiIsolate()
    return when (details.plan) {
        ProPlan.YEARLY -> stringResource(R.string.pro_price_per_year, price)
        ProPlan.WEEKLY -> stringResource(R.string.pro_price_per_week, price)
        ProPlan.LIFETIME -> price
    }
}

@Composable
internal fun proPlanBadge(option: ProPlanOption, isRecommended: Boolean): String? = when {
    option.details.isOneTime -> stringResource(R.string.pro_badge_one_time)
    option.savingsPercent != null -> stringResource(R.string.pro_save_percent, option.savingsPercent)
    isRecommended -> stringResource(R.string.pro_best_value)
    else -> null
}

@Composable
internal fun proCtaText(state: ProPlansState): String = stringResource(
    when {
        state is ProPlansState.Unavailable -> R.string.pro_retry
        state is ProPlansState.Content && state.startsWithTrial -> R.string.pro_cta_access_free
        else -> R.string.pro_cta_continue
    },
)

@Composable
internal fun proFootnote(state: ProPlansState): String = when (state) {
    ProPlansState.Unavailable -> stringResource(R.string.pro_store_unavailable)
    ProPlansState.Loading -> stringResource(R.string.pro_auto_renewal_note)
    is ProPlansState.Content -> {
        val price = state.selectedPlan.formattedPrice.bidiIsolate()
        when (state.selectedPlan.plan) {
            ProPlan.YEARLY -> stringResource(R.string.pro_renewal_note_yearly, price)
            ProPlan.WEEKLY -> stringResource(R.string.pro_renewal_note_weekly, price)
            ProPlan.LIFETIME -> stringResource(R.string.pro_footnote_lifetime, price)
        }
    }
}

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
