package com.aipose.camera.posematch.ui.screens.pro.components

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import com.aipose.camera.posematch.R
import com.aipose.camera.posematch.domain.models.PremiumPlan
import com.aipose.camera.posematch.domain.models.ProPlan
import com.aipose.camera.posematch.ui.screens.pro.models.ProEvent
import com.aipose.camera.posematch.ui.screens.pro.models.ProPlansState
import com.aipose.camera.posematch.util.bidiIsolate

@Composable
internal fun proPlanTitle(plan: ProPlan): String = stringResource(
    when (plan) {
        ProPlan.LIFETIME -> R.string.pro_plan_lifetime
        ProPlan.YEARLY -> R.string.pro_plan_yearly
    },
)

@Composable
internal fun proPlanPrice(details: PremiumPlan): String = details.formattedPrice.bidiIsolate()

@Composable
internal fun proPlanSubtitle(plan: ProPlan, details: PremiumPlan?): String {
    if (details == null) return stringResource(R.string.pro_plan_unavailable)
    return when {
        details.hasTrial -> pluralStringResource(
            R.plurals.pro_trial_then_price,
            details.trialDays,
            details.trialDays,
            details.formattedPrice.bidiIsolate(),
        )

        plan == ProPlan.LIFETIME -> stringResource(R.string.pro_plan_lifetime_note)
        else -> stringResource(R.string.pro_plan_yearly_note)
    }
}

@Composable
internal fun proCtaText(state: ProPlansState): String = stringResource(
    when {
        state is ProPlansState.Unavailable -> R.string.pro_retry
        state is ProPlansState.Content && state.startsWithTrial -> R.string.pro_try_for_free
        state is ProPlansState.Content && !state.selectedPlan.isOneTime -> R.string.pro_cta_yearly
        else -> R.string.pro_cta_lifetime
    },
)

@Composable
internal fun proFootnote(state: ProPlansState): String = stringResource(
    when {
        state is ProPlansState.Unavailable -> R.string.pro_store_unavailable
        state is ProPlansState.Content && !state.selectedPlan.isOneTime ->
            R.string.pro_auto_renewal_note

        else -> R.string.pro_footnote_lifetime
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
