package com.aipose.camera.posematch.ui.screens.bottomBar

import com.aipose.camera.posematch.R
import com.aipose.camera.posematch.ui.graph.NavRoute

sealed class BottomNavItem(
    val title: Int,
    val icon: Int,
    val iconFilled: Int,
    val route: String
) {
    data object HomeItem : BottomNavItem(
        title = R.string.nav_home,
        icon = R.drawable.bottom_home,
        iconFilled = R.drawable.bottom_home_filled,
        route = NavRoute.HomeScreenRoute.route
    )

    data object CollectionsItem : BottomNavItem(
        title = R.string.nav_gallery,
        icon = R.drawable.bottom_collections,
        iconFilled = R.drawable.bottom_collections_filled,
        route = NavRoute.CollectionsScreenRoute.route
    )

    data object SettingItem : BottomNavItem(
        title = R.string.nav_settings,
        icon = R.drawable.bottom_settings,
        iconFilled = R.drawable.bottom_settings_filled,
        route = NavRoute.SettingScreenRoute.route
    )

    companion object {

        val ordered: List<BottomNavItem> = listOf(
            HomeItem,
            CollectionsItem,
            SettingItem,
        )

        val tabIndices: Map<String, Int> =
            ordered.withIndex().associate { (index, item) -> item.route to index }
    }
}
