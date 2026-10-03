package com.aipose.camera.posematch.ui.common

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.navigation.NamedNavArgument
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.aipose.camera.posematch.ui.screens.bottomBar.BottomNavItem

fun NavGraphBuilder.addScreenWithTransitions(
    enterDuration: Int = 700,
    exitDuration: Int = 700,
    popEnterDuration: Int = 700,
    popExitDuration: Int = 700,
    route: String,
    arguments: List<NamedNavArgument> = emptyList(),
    tabIndexMap: Map<String, Int>? = BottomNavItem.tabIndices,
    contentBack: @Composable AnimatedContentScope.(NavBackStackEntry) -> Unit
) {
    composable(
        route = route,
        arguments = arguments,
        enterTransition = {
            val direction = resolveSlideDirection(
                from = initialState.destination,
                to = targetState.destination,
                tabIndexMap = tabIndexMap,
                defaultDirection = AnimatedContentTransitionScope.SlideDirection.Left
            )
            slideIntoContainer(direction, animationSpec = tween(enterDuration))
        },
        exitTransition = {
            val direction = resolveSlideDirection(
                from = initialState.destination,
                to = targetState.destination,
                tabIndexMap = tabIndexMap,
                defaultDirection = AnimatedContentTransitionScope.SlideDirection.Left
            )
            slideOutOfContainer(direction, animationSpec = tween(exitDuration))
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
        content = contentBack
    )
}

private fun resolveSlideDirection(
    from: NavDestination?,
    to: NavDestination?,
    tabIndexMap: Map<String, Int>?,
    defaultDirection: AnimatedContentTransitionScope.SlideDirection
): AnimatedContentTransitionScope.SlideDirection {
    if (tabIndexMap == null) return defaultDirection
    val fromIndex = from.tabIndexIn(tabIndexMap) ?: return defaultDirection
    val toIndex = to.tabIndexIn(tabIndexMap) ?: return defaultDirection

    return when {
        toIndex > fromIndex -> AnimatedContentTransitionScope.SlideDirection.Left
        toIndex < fromIndex -> AnimatedContentTransitionScope.SlideDirection.Right
        else -> defaultDirection
    }
}

private fun NavDestination?.tabIndexIn(tabIndexMap: Map<String, Int>): Int? =
    this?.hierarchy?.firstNotNullOfOrNull { tabIndexMap[it.route] }
