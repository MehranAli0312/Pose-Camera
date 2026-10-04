package com.aipose.camera.posematch.ui.screens.bottomBar

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.ui.graphics.Color
import com.aipose.camera.posematch.R
import com.aipose.camera.posematch.ui.graph.NavRoute
import com.aipose.camera.posematch.ui.theme.Indigo
import com.aipose.camera.posematch.ui.theme.PoseIndigoLight
import com.aipose.camera.posematch.ui.theme.PoseNavActive
import com.aipose.camera.posematch.ui.theme.PosePink
import com.aipose.camera.posematch.ui.theme.PosePinkLabel
import com.aipose.camera.posematch.ui.theme.PosePinkSoft

sealed class BottomNavItem(
    @StringRes val title: Int,
    @DrawableRes val icon: Int,
    val route: String,
    val pillColor: Color,
    val activeIconColor: Color,
    val activeLabelColor: Color
) {
    data object HomeItem : BottomNavItem(
        title = R.string.nav_home,
        icon = R.drawable.ic_pose_nav_home,
        route = NavRoute.HomeScreenRoute.route,
        pillColor = Indigo,
        activeIconColor = PoseNavActive,
        activeLabelColor = PoseIndigoLight
    )

    data object CollectionsItem : BottomNavItem(
        title = R.string.nav_gallery,
        icon = R.drawable.ic_pose_nav_collections,
        route = NavRoute.CollectionsScreenRoute.route,
        pillColor = Indigo,
        activeIconColor = PoseNavActive,
        activeLabelColor = PoseIndigoLight
    )

    data object SavedItem : BottomNavItem(
        title = R.string.nav_saved,
        icon = R.drawable.ic_pose_nav_saved,
        route = NavRoute.SavedScreenRoute.route,
        pillColor = PosePink,
        activeIconColor = PosePinkSoft,
        activeLabelColor = PosePinkLabel
    )

    data object SettingItem : BottomNavItem(
        title = R.string.nav_settings,
        icon = R.drawable.ic_pose_nav_settings,
        route = NavRoute.SettingScreenRoute.route,
        pillColor = Indigo,
        activeIconColor = PoseNavActive,
        activeLabelColor = PoseIndigoLight
    )

    companion object {

        val leading: List<BottomNavItem> = listOf(HomeItem, CollectionsItem)

        val trailing: List<BottomNavItem> = listOf(SavedItem, SettingItem)

        val ordered: List<BottomNavItem> = leading + trailing

        val tabIndices: Map<String, Int> =
            ordered.withIndex().associate { (index, item) -> item.route to index }
    }
}
