package com.aipose.camera.posematch.ui.screens.bottomBar

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import com.aipose.camera.posematch.ui.animation.AppAnimation
import com.aipose.camera.posematch.ui.common.bounceClick
import com.aipose.camera.posematch.ui.graph.acceptNavigationClick

@Composable
fun BottomNavigationBar(
    navController: NavController,
    selectedDestination: NavDestination?,
) {
    val items = BottomNavItem.ordered
    AnimatedVisibility(
        visible = true, enter = slideInVertically(
            initialOffsetY = { it },
            animationSpec = tween(AppAnimation.TRANSITION_ANIMATION_DURATION)
        ), exit = slideOutVertically(
            targetOffsetY = { it },
            animationSpec = tween(AppAnimation.TRANSITION_ANIMATION_DURATION)
        ), content = {

            NavigationBar(
                modifier = Modifier.fillMaxSize(),
                containerColor = Color.Transparent
            ) {
                items.forEach { item ->
                    val selected =
                        selectedDestination?.hierarchy?.any { it.route == item.route } == true

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(56.dp)
                            .bounceClick {
                                if (navController.acceptNavigationClick()) {
                                    navController.navigate(item.route) {
                                        popUpTo(navController.graph.id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            }) {

                        Icon(
                            modifier = Modifier.wrapContentSize(),
                            painter = if (selected) painterResource(item.iconFilled) else painterResource(
                                item.icon
                            ),
                            contentDescription = stringResource(id = item.title),
                            tint = if (selected) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = stringResource(id = item.title),
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = if (selected) FontWeight.Medium else FontWeight.Normal,
                                color = if (selected) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                },
                            ),
                            maxLines = 1,
                            modifier = Modifier.basicMarquee(),
                        )
                    }

                }
            }

        })
}
