package com.aipose.camera.posematch.ui.screens.bottomBar

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import com.aipose.camera.posematch.R
import com.aipose.camera.posematch.ads.rememberInnerInterstitial
import com.aipose.camera.posematch.ui.common.CaptureFab
import com.aipose.camera.posematch.ui.common.click
import com.aipose.camera.posematch.ui.common.poseRaisedCard
import com.aipose.camera.posematch.ui.graph.NavRoute
import com.aipose.camera.posematch.ui.graph.acceptNavigationClick
import com.aipose.camera.posematch.ui.graph.navigateToTab
import com.aipose.camera.posematch.ui.models.GlossyBadgePalette
import com.aipose.camera.posematch.ui.theme.LocalAppPalette
import com.aipose.camera.posematch.ui.theme.Violet
import com.aipose.camera.posematch.ui.theme.poseTextStyle

private val BottomBarHeight = 90.dp
private val BarHeight = 70.dp
private val BarCorner = 26.dp
private val BarHorizontalMargin = 12.dp
private val BarInnerPadding = 14.dp
private val PillWidth = 32.dp
private val PillHeight = 30.dp
private val PillShape = RoundedCornerShape(12.dp)
private val IconSize = 21.dp
private val FabSlotWidth = 74.dp
private val FabSize = 60.dp
private val FabGlowSize = 76.dp
private val FabTopInset = 2.dp

private const val BAR_SHADOW_ALPHA = 0.5f
private const val PILL_ALPHA = 0.18f
private const val FAB_GLOW_ALPHA = 0.18f

@Composable
fun BottomNavigationBar(
    navController: NavController,
    selectedDestination: NavDestination?,
    modifier: Modifier = Modifier,
) {
    val palette = LocalAppPalette.current
    val innerInterstitial = rememberInnerInterstitial()
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(BottomBarHeight),
    ) {
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = BarHorizontalMargin)
                .height(BarHeight)
                .poseRaisedCard(
                    cornerRadius = BarCorner,
                    brush = SolidColor(palette.navSurface),
                    shadowColor = palette.navShadow,
                    shadowAlpha = BAR_SHADOW_ALPHA,
                    borderColor = palette.cardBorder,
                )
                .padding(horizontal = BarInnerPadding),
        ) {
            BottomNavItem.leading.forEach { item ->
                NavTab(
                    item = item,
                    selectedDestination = selectedDestination,
                    navController = navController,
                )
            }
            Spacer(modifier = Modifier.width(FabSlotWidth))
            BottomNavItem.trailing.forEach { item ->
                NavTab(
                    item = item,
                    selectedDestination = selectedDestination,
                    navController = navController,
                )
            }
        }
        CaptureFab(
            onClick = {
                if (navController.acceptNavigationClick()) {
                    val route = NavRoute.CameraScreenRoute.routeWithoutPose()
                    innerInterstitial.showThen { navController.navigate(route) }
                }
            },
            glowColor = Violet,
            glowAlpha = FAB_GLOW_ALPHA,
            contentDescription = stringResource(R.string.nav_capture),
            palette = GlossyBadgePalette.NavCapture,
            buttonSize = FabSize,
            glowSize = FabGlowSize,
            glyphSize = IconSize,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = FabTopInset),
        )
    }
}

@Composable
private fun RowScope.NavTab(
    item: BottomNavItem,
    selectedDestination: NavDestination?,
    navController: NavController,
) {
    val palette = LocalAppPalette.current
    val selected = selectedDestination?.hierarchy?.any { it.route == item.route } == true
    val label = stringResource(item.title)
    Column(
        modifier = Modifier
            .weight(1f)
            .click { navController.navigateToTab(item.route) },
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.height(12.dp))
        Box(
            modifier = Modifier
                .size(width = PillWidth, height = PillHeight)
                .clip(PillShape)
                .background(
                    if (selected) item.pillColor.copy(alpha = PILL_ALPHA) else Color.Transparent
                ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(item.icon),
                contentDescription = label,
                tint = if (selected) item.activeIconColor else palette.navInactive,
                modifier = Modifier.size(IconSize),
            )
        }
        Spacer(modifier = Modifier.height(3.dp))
        Text(
            text = label,
            style = poseTextStyle(
                9.sp,
                FontWeight.Bold,
                if (selected) item.activeLabelColor else palette.navInactive,
            ),
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
