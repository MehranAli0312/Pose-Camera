package com.aipose.camera.posematch.ui.common

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

object AppInsets {

    val safeTopSystemBars: WindowInsets
        @Composable
        get() = WindowInsets.systemBars
            .union(WindowInsets.displayCutout)
            .only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal)

    val safeBottomSystemBars: WindowInsets
        @Composable
        get() = WindowInsets.systemBars
            .union(WindowInsets.displayCutout)
            .only(WindowInsetsSides.Bottom + WindowInsetsSides.Horizontal)
}

@Composable
fun Modifier.safeTopSystemBarsPadding(): Modifier =
    windowInsetsPadding(AppInsets.safeTopSystemBars)

@Composable
fun Modifier.safeBottomSystemBarsPadding(): Modifier =
    windowInsetsPadding(AppInsets.safeBottomSystemBars)
