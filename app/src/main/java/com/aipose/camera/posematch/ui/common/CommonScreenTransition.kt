package com.aipose.camera.posematch.ui.common

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.navigation.NamedNavArgument
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable

private const val SCREEN_TRANSITION_MILLIS = 320

fun NavGraphBuilder.addScreenWithTransitions(
    enterDuration: Int = SCREEN_TRANSITION_MILLIS,
    exitDuration: Int = SCREEN_TRANSITION_MILLIS,
    popEnterDuration: Int = SCREEN_TRANSITION_MILLIS,
    popExitDuration: Int = SCREEN_TRANSITION_MILLIS,
    route: String,
    arguments: List<NamedNavArgument> = emptyList(),
    contentBack: @Composable AnimatedContentScope.(NavBackStackEntry) -> Unit
) {
    composable(
        route = route,
        arguments = arguments,
        enterTransition = {
            slideIntoContainer(
                AnimatedContentTransitionScope.SlideDirection.Left,
                animationSpec = tween(enterDuration),
            )
        },
        exitTransition = {
            slideOutOfContainer(
                AnimatedContentTransitionScope.SlideDirection.Left,
                animationSpec = tween(exitDuration),
            )
        },
        popEnterTransition = {
            slideIntoContainer(
                AnimatedContentTransitionScope.SlideDirection.Right,
                animationSpec = tween(popEnterDuration)
            )
        },
        popExitTransition = {
            slideOutOfContainer(
                AnimatedContentTransitionScope.SlideDirection.Right,
                animationSpec = tween(popExitDuration)
            )
        },
    ) { backStackEntry ->
        Box(modifier = Modifier.fillMaxSize().graphicsLayer()) {
            contentBack(backStackEntry)
        }
    }
}
