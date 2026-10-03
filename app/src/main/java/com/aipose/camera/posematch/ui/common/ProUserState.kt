package com.aipose.camera.posematch.ui.common

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.ads.AdsManager
import org.koin.compose.koinInject

@Composable
fun isProUser(): Boolean {
    val adsManager: AdsManager = koinInject()
    val isPro by adsManager.isPro.collectAsState()
    return isPro
}
