package com.aipose.camera.posematch.ui.screens.pro.models

import com.aipose.camera.posematch.ui.firebaseRemote.PremiumCloseButtonPosition

data class ProUiState(
    val plans: ProPlansState,
    val isRestoring: Boolean,
    val closeSecondsRemaining: Int,
    val closePosition: PremiumCloseButtonPosition,
) {
    private val isPurchasing: Boolean
        get() = (plans as? ProPlansState.Content)?.isPurchasing == true

    val canRestore: Boolean get() = !isPurchasing && !isRestoring

    val canClose: Boolean get() = closeSecondsRemaining <= 0
}
