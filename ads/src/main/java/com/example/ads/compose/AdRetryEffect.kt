package com.example.ads.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.lifecycle.compose.LifecycleResumeEffect
import com.example.ads.internal.AdRetryState
import com.example.ads.internal.NetworkChecker
import org.koin.compose.koinInject

@Composable
internal fun AdRetryEffect(
    failed: Boolean,
    retryState: AdRetryState,
    onRetry: () -> Unit,
) {
    if (!failed) return

    val network: NetworkChecker = koinInject()
    val currentOnRetry by rememberUpdatedState(onRetry)

    LaunchedEffect(network, retryState) {
        network.networkRestored().collect {
            if (retryState.tryRetry(requireGap = false)) currentOnRetry()
        }
    }

    LifecycleResumeEffect(network, retryState) {
        if (network.isOnline() && retryState.tryRetry(requireGap = true)) currentOnRetry()
        onPauseOrDispose { }
    }
}
