package com.aipose.camera.posematch.ui.screens.components

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aipose.camera.posematch.R
import com.aipose.camera.posematch.ui.screens.splash.components.SplashAmbientBackground
import com.aipose.camera.posematch.ui.screens.splash.components.SplashBrandMark
import com.aipose.camera.posematch.ui.theme.poseTextStyle

private val BrandToTitleSpacing = 28.dp
private val HorizontalPadding = 24.dp
private val TitleSize = 20.sp
private val ProgressSize = 32.dp
private val ProgressStrokeWidth = 3.dp

@Composable
fun AppOpenLoadingOverlay(modifier: Modifier = Modifier) {
    BackHandler {}

    SplashAmbientBackground(
        modifier = modifier.pointerInput(Unit) {
            awaitEachGesture { awaitPointerEvent().changes.forEach { it.consume() } }
        },
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = HorizontalPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(
                space = BrandToTitleSpacing,
                alignment = Alignment.CenterVertically,
            ),
        ) {
            SplashBrandMark()
            Text(
                text = stringResource(R.string.app_open_welcome_back),
                style = poseTextStyle(TitleSize, FontWeight.Bold, MaterialTheme.colorScheme.onBackground),
                textAlign = TextAlign.Center,
            )
            CircularProgressIndicator(
                color = MaterialTheme.colorScheme.primary,
                strokeWidth = ProgressStrokeWidth,
                modifier = Modifier.size(ProgressSize),
            )
        }
    }
}
