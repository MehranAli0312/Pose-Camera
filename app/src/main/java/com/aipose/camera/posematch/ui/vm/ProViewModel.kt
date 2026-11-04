package com.aipose.camera.posematch.ui.vm

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aipose.camera.posematch.ads.ProRestoreResult
import com.aipose.camera.posematch.domain.models.PaywallAnalyticsAction
import com.aipose.camera.posematch.domain.models.PaywallAnalyticsEvent
import com.aipose.camera.posematch.domain.models.PremiumPlan
import com.aipose.camera.posematch.domain.models.PremiumPurchaseOutcome
import com.aipose.camera.posematch.domain.usecase.PremiumSubscriptionUseCase
import com.aipose.camera.posematch.domain.usecase.TrackEventUseCase
import com.aipose.camera.posematch.ui.common.PremiumPurchaseLauncher
import com.aipose.camera.posematch.ui.firebaseRemote.AdsRemoteConfigStore
import com.aipose.camera.posematch.ui.screens.pro.models.ProEvent
import com.aipose.camera.posematch.ui.screens.pro.models.ProPlanOption
import com.aipose.camera.posematch.ui.screens.pro.models.ProPlansState
import com.aipose.camera.posematch.ui.screens.pro.models.ProUiState
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ProViewModel(
    private val subscriptionUseCase: PremiumSubscriptionUseCase,
    private val purchaseLauncher: PremiumPurchaseLauncher,
    private val remoteConfigStore: AdsRemoteConfigStore,
    private val trackEventUseCase: TrackEventUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(initialState())
    val uiState: StateFlow<ProUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<ProEvent>(extraBufferCapacity = 1)
    val events: SharedFlow<ProEvent> = _events.asSharedFlow()

    private var loadJob: Job? = null
    private var restoreJob: Job? = null

    init {
        trackPaywall(PaywallAnalyticsAction.Viewed)
        startCloseCountdown()
        observePurchaseOutcomes()
        loadPlans()
    }

    fun loadPlans() {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            _uiState.update { it.copy(plans = ProPlansState.Loading) }
            val plans = subscriptionUseCase.loadPlans().getOrDefault(emptyList())
            val selected = subscriptionUseCase.defaultSelection(plans)
            val plansState = if (selected == null) {
                ProPlansState.Unavailable
            } else {
                ProPlansState.Content(
                    options = plans.map { plan ->
                        ProPlanOption(
                            details = plan,
                            savingsPercent = subscriptionUseCase.savingsPercent(plan, plans),
                        )
                    },
                    selectedPlan = selected,
                    recommendedPlan = plans.firstOrNull {
                        subscriptionUseCase.isRecommended(it, plans)
                    },
                    isPurchasing = false,
                )
            }
            _uiState.update { it.copy(plans = plansState) }
        }
    }

    fun selectPlan(plan: PremiumPlan) {
        val content = _uiState.value.plans as? ProPlansState.Content ?: return
        if (content.isPurchasing || content.selectedPlan == plan) return
        updateContent { it.copy(selectedPlan = plan) }
        trackPaywall(PaywallAnalyticsAction.PlanSelected)
    }

    fun startPurchase(activity: Activity?) {
        val state = _uiState.value
        val content = state.plans as? ProPlansState.Content ?: return
        if (content.isPurchasing || state.isRestoring) return
        trackPaywall(PaywallAnalyticsAction.PurchaseClicked)
        updateContent { it.copy(isPurchasing = true) }
        if (!purchaseLauncher.launch(activity, content.selectedPlan)) {
            updateContent { it.copy(isPurchasing = false) }
            trackPaywall(PaywallAnalyticsAction.PurchaseFailed)
            _events.tryEmit(ProEvent.PurchaseFailed(reason = ""))
        }
    }

    fun restorePurchases() {
        if (!_uiState.value.canRestore) return
        restoreJob?.cancel()
        _uiState.update { it.copy(isRestoring = true) }
        restoreJob = viewModelScope.launch {
            val result = subscriptionUseCase.restorePurchases()
            _uiState.update { it.copy(isRestoring = false) }
            if (result == ProRestoreResult.Restored) trackPaywall(PaywallAnalyticsAction.Restored)
            _events.tryEmit(result.toEvent())
        }
    }

    fun onClosed() = trackPaywall(PaywallAnalyticsAction.Closed)

    private fun startCloseCountdown() {
        viewModelScope.launch {
            while (_uiState.value.closeSecondsRemaining > 0) {
                delay(CLOSE_COUNTDOWN_TICK_MILLIS)
                _uiState.update { it.copy(closeSecondsRemaining = it.closeSecondsRemaining - 1) }
            }
        }
    }

    private fun observePurchaseOutcomes() {
        viewModelScope.launch {
            subscriptionUseCase.purchaseOutcomes.collect { outcome ->
                updateContent { it.copy(isPurchasing = false) }
                outcome.toAnalyticsAction()?.let(::trackPaywall)
                _events.tryEmit(outcome.toEvent())
            }
        }
    }

    private fun trackPaywall(action: PaywallAnalyticsAction) {
        val selectedPlan = (_uiState.value.plans as? ProPlansState.Content)?.selectedPlan?.plan
        viewModelScope.launch {
            trackEventUseCase.trackPaywall(PaywallAnalyticsEvent(action = action, plan = selectedPlan))
        }
    }

    private fun updateContent(transform: (ProPlansState.Content) -> ProPlansState.Content) {
        _uiState.update { state ->
            val plans = state.plans
            if (plans is ProPlansState.Content) state.copy(plans = transform(plans)) else state
        }
    }

    private fun initialState() = ProUiState(
        plans = ProPlansState.Loading,
        isRestoring = false,
        closeSecondsRemaining = remoteConfigStore.current.premiumCloseDelay,
        closePosition = remoteConfigStore.current.premiumClosePosition,
    )

    private companion object {
        const val CLOSE_COUNTDOWN_TICK_MILLIS = 1_000L
    }
}

private fun ProRestoreResult.toEvent(): ProEvent = when (this) {
    ProRestoreResult.Restored -> ProEvent.Restored
    ProRestoreResult.NothingFound -> ProEvent.NothingToRestore
    ProRestoreResult.Failed -> ProEvent.RestoreFailed
}

private fun PremiumPurchaseOutcome.toEvent(): ProEvent = when (this) {
    PremiumPurchaseOutcome.Purchased -> ProEvent.Purchased
    PremiumPurchaseOutcome.AlreadyOwned -> ProEvent.AlreadyOwned
    PremiumPurchaseOutcome.Pending -> ProEvent.PurchasePending
    PremiumPurchaseOutcome.Cancelled -> ProEvent.PurchaseCancelled
    is PremiumPurchaseOutcome.Failed -> ProEvent.PurchaseFailed(reason)
}

private fun PremiumPurchaseOutcome.toAnalyticsAction(): PaywallAnalyticsAction? = when (this) {
    PremiumPurchaseOutcome.Purchased -> PaywallAnalyticsAction.Purchased
    PremiumPurchaseOutcome.Pending -> PaywallAnalyticsAction.PurchasePending
    PremiumPurchaseOutcome.Cancelled -> PaywallAnalyticsAction.PurchaseCancelled
    is PremiumPurchaseOutcome.Failed -> PaywallAnalyticsAction.PurchaseFailed
    PremiumPurchaseOutcome.AlreadyOwned -> null
}
