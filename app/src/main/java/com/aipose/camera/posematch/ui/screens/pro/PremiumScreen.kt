package com.aipose.camera.posematch.ui.screens.pro

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.aipose.camera.posematch.ui.common.getActivity
import com.aipose.camera.posematch.ui.common.safeBottomSystemBarsPadding
import com.aipose.camera.posematch.ui.common.safeTopSystemBarsPadding
import com.aipose.camera.posematch.ui.graph.popBackStackOnClick
import com.aipose.camera.posematch.ui.screens.pro.components.ProBenefitsCard
import com.aipose.camera.posematch.ui.screens.pro.components.ProBottomCta
import com.aipose.camera.posematch.ui.screens.pro.components.ProHeader
import com.aipose.camera.posematch.ui.screens.pro.components.ProPlanSelector
import com.aipose.camera.posematch.ui.screens.pro.components.ProTopBar
import com.aipose.camera.posematch.ui.screens.pro.components.ProTopBarHeight
import com.aipose.camera.posematch.ui.screens.pro.components.proEventMessage
import com.aipose.camera.posematch.ui.screens.pro.components.proPaywallBackground
import com.aipose.camera.posematch.ui.vm.ProViewModel
import com.aipose.camera.posematch.util.PRIVACY_POLICY
import com.aipose.camera.posematch.util.TERMS_OF_SERVICE
import com.example.common.showToast
import org.koin.androidx.compose.koinViewModel

private val PaywallGutter = 16.dp

@Composable
fun PremiumScreen(
    navController: NavHostController,
    viewModel: ProViewModel = koinViewModel(),
    onClose: () -> Unit = { navController.popBackStackOnClick() },
    onPurchased: () -> Unit = onClose,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current
    val activity = getActivity()
    val currentOnPurchased by rememberUpdatedState(onPurchased)

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            context.showToast(context.proEventMessage(event))
            if (event.closesPaywall) currentOnPurchased()
        }
    }

    BackHandler {
        if (state.canClose) onClose()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .proPaywallBackground()
            .safeBottomSystemBarsPadding(),
    ) {
        Box(modifier = Modifier.weight(1f)) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .safeTopSystemBarsPadding()
                    .padding(horizontal = PaywallGutter),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Spacer(Modifier.height(ProTopBarHeight + 22.dp))

                ProHeader()

                Spacer(Modifier.height(19.dp))

                ProBenefitsCard()

                Spacer(Modifier.height(6.dp))

                ProPlanSelector(
                    state = state.plans,
                    onSelectPlan = viewModel::selectPlan,
                )
            }

            ProTopBar(
                closeSecondsRemaining = state.closeSecondsRemaining,
                closePosition = state.closePosition,
                onClose = onClose,
                modifier = Modifier.safeTopSystemBarsPadding(),
            )
        }

        ProBottomCta(
            state = state.plans,
            isRestoring = state.isRestoring,
            canRestore = state.canRestore,
            onContinue = { viewModel.startPurchase(activity) },
            onRetry = viewModel::loadPlans,
            onRestore = viewModel::restorePurchases,
            onTerms = { uriHandler.openUri(TERMS_OF_SERVICE) },
            onPrivacyPolicy = { uriHandler.openUri(PRIVACY_POLICY) },
            modifier = Modifier.padding(
                start = PaywallGutter,
                end = PaywallGutter,
                top = 28.dp,
                bottom = 24.dp,
            ),
        )
    }
}
