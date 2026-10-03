package com.aipose.camera.posematch.ui.screens.pro

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.example.common.showToast
import com.pdfutility.billing.presentation.states.PurchaseResult
import com.aipose.camera.posematch.ads.ProRestoreResult
import com.aipose.camera.posematch.ui.common.getActivity
import com.aipose.camera.posematch.ui.graph.popBackStackOnClick
import com.aipose.camera.posematch.ui.screens.pro.components.ProBottomCta
import com.aipose.camera.posematch.ui.screens.pro.components.ProHeader
import com.aipose.camera.posematch.ui.screens.pro.components.ProIncludedFeatures
import com.aipose.camera.posematch.ui.screens.pro.components.ProPlanSelector
import com.aipose.camera.posematch.ui.screens.pro.components.ProTopBar
import com.aipose.camera.posematch.ui.screens.pro.components.proAmbientGlow
import com.aipose.camera.posematch.ui.screens.pro.components.proPurchaseMessage
import com.aipose.camera.posematch.ui.screens.pro.components.proRestoreMessage
import com.aipose.camera.posematch.ui.vm.ProViewModel
import com.aipose.camera.posematch.util.PRIVACY_POLICY
import org.koin.androidx.compose.koinViewModel

@Composable
fun ProScreen(
    navController: NavHostController,
    viewModel: ProViewModel = koinViewModel(),
    onClose: () -> Unit = { navController.popBackStackOnClick() },
    onPurchased: () -> Unit = onClose,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current
    val activity = getActivity()
    val purchaseMessage = state.purchaseResult?.let { proPurchaseMessage(it) }
    val restoreMessage = state.restoreResult?.let { proRestoreMessage(it) }


    LaunchedEffect(state.purchaseResult) {
        val result = state.purchaseResult ?: return@LaunchedEffect
        purchaseMessage?.let(context::showToast)
        viewModel.consumePurchaseResult()
        if (result is PurchaseResult.Success || result is PurchaseResult.Pending) {
            onPurchased()
        }
    }

    LaunchedEffect(state.restoreResult) {
        val result = state.restoreResult ?: return@LaunchedEffect
        restoreMessage?.let(context::showToast)
        viewModel.consumeRestoreResult()
        if (result == ProRestoreResult.Restored) {
            onPurchased()
        }
    }

    BackHandler {
        if (state.canClose) onClose()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .proAmbientGlow()
            .statusBarsPadding()
            .navigationBarsPadding(),
    ) {
        ProTopBar(
            closeSecondsRemaining = state.closeSecondsRemaining,
            closePosition = state.closePosition,
            onClose = onClose,
            canRestore = state.canRestore,
            onRestore = viewModel::restorePurchases,
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            ProHeader()

            Spacer(Modifier.height(20.dp))

            ProIncludedFeatures()

            Spacer(Modifier.height(20.dp))

            ProPlanSelector(
                state = state,
                onSelectPlan = viewModel::selectPlan,
            )

            Spacer(Modifier.height(16.dp))
        }

        ProBottomCta(
            state = state,
            onContinue = { viewModel.purchaseSelectedPlan(activity) },
            onRetry = viewModel::retryLoadProducts,
            onPrivacyPolicy = { uriHandler.openUri(PRIVACY_POLICY) },
            modifier = Modifier.padding(top = 8.dp, bottom = 16.dp),
        )
    }
}
