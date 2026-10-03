package com.aipose.camera.posematch.ui.screens.onboard.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aipose.camera.posematch.ui.screens.onboard.models.OnboardSlide

private const val ENTER_DURATION_MS = 320
private const val EXIT_DURATION_MS = 160
private const val ENTER_SLIDE_DIVISOR = 6

@Composable
internal fun OnboardSlideText(
    slide: OnboardSlide?,
    modifier: Modifier = Modifier,
) {
    AnimatedContent(
        targetState = slide,
        transitionSpec = {
            (fadeIn(tween(ENTER_DURATION_MS)) + slideInVertically(tween(ENTER_DURATION_MS)) { it / ENTER_SLIDE_DIVISOR })
                .togetherWith(fadeOut(tween(EXIT_DURATION_MS)))
                .using(SizeTransform(clip = false))
        },
        contentAlignment = Alignment.TopCenter,
        label = "onboardSlideText",
        modifier = modifier,
    ) { target ->
        if (target != null) {
            OnboardSlideCopy(slide = target)
        } else {
            Spacer(modifier = Modifier.fillMaxWidth())
        }
    }
}

@Composable
private fun OnboardSlideCopy(slide: OnboardSlide) {
    val titleStyle = MaterialTheme.typography.headlineLarge.copy(
        fontSize = 30.sp,
        lineHeight = 36.sp,
    )
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = stringResource(slide.titleRes),
            style = titleStyle,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(14.dp))
        Text(
            text = stringResource(slide.descriptionRes),
            style = MaterialTheme.typography.bodyLarge.copy(
                fontSize = 15.sp,
                lineHeight = 23.sp,
            ),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            minLines = 3,
            modifier = Modifier.padding(horizontal = 12.dp),
        )
    }
}
