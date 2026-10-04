package com.aipose.camera.posematch.ui.graph

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.navArgument
import com.aipose.camera.posematch.ui.common.addScreenWithTransitions
import com.aipose.camera.posematch.ui.screens.bottomBar.InScreenBottomBarHost
import com.aipose.camera.posematch.ui.screens.camera.PoseCameraScreen
import com.aipose.camera.posematch.ui.screens.captureAlbum.CaptureAlbumScreen
import com.aipose.camera.posematch.ui.screens.captureDetail.CaptureDetailScreen
import com.aipose.camera.posematch.ui.screens.collections.CollectionsScreen
import com.aipose.camera.posematch.ui.screens.home.HomeScreen
import com.aipose.camera.posematch.ui.screens.language.LocalizeScreen
import com.aipose.camera.posematch.ui.screens.photoEdit.PhotoEditScreen
import com.aipose.camera.posematch.ui.screens.photoSuccess.PhotoSuccessScreen
import com.aipose.camera.posematch.ui.screens.poseAlbum.PoseAlbumScreen
import com.aipose.camera.posematch.ui.screens.poseDetail.PoseDetailScreen
import com.aipose.camera.posematch.ui.screens.pro.ProScreen
import com.aipose.camera.posematch.ui.screens.saved.SavedScreen
import com.aipose.camera.posematch.ui.screens.settings.SettingScreen

@Composable
fun DashboardNavGraph(
    navController: NavHostController,
) {
    NavHost(
        modifier = Modifier,
        navController = navController,
        startDestination = NavRoute.HomeScreenRoute.route
    ) {
        addHomeScreen(navController, this)
        addCollectionsScreen(navController, this)
        addSavedScreen(navController, this)
        addSettingScreen(navController, this)
        addLanguageScreen(navController, this)
        addProScreen(navController, this)
        addCameraScreen(navController, this)
        addPoseAlbumScreen(navController, this)
        addPoseDetailScreen(navController, this)
        addCaptureAlbumScreen(navController, this)
        addCaptureDetailScreen(navController, this)
        addPhotoEditScreen(navController, this)
        addPhotoSuccessScreen(navController, this)
    }
}

private fun addHomeScreen(
    navController: NavHostController, navGraphBuilder: NavGraphBuilder
) {
    navGraphBuilder.addScreenWithTransitions(
        route = NavRoute.HomeScreenRoute.route,
    ) { backStackEntry ->
        InScreenBottomBarHost(destination = backStackEntry.destination) {
            HomeScreen(navController = navController)
        }
    }
}

private fun addCollectionsScreen(
    navController: NavHostController, navGraphBuilder: NavGraphBuilder
) {
    navGraphBuilder.addScreenWithTransitions(
        route = NavRoute.CollectionsScreenRoute.route,
    ) { backStackEntry ->
        InScreenBottomBarHost(destination = backStackEntry.destination) {
            CollectionsScreen(navController = navController)
        }
    }
}

private fun addSavedScreen(
    navController: NavHostController, navGraphBuilder: NavGraphBuilder
) {
    navGraphBuilder.addScreenWithTransitions(
        route = NavRoute.SavedScreenRoute.route,
    ) { backStackEntry ->
        InScreenBottomBarHost(destination = backStackEntry.destination) {
            SavedScreen(navController = navController)
        }
    }
}

private fun addSettingScreen(
    navController: NavHostController, navGraphBuilder: NavGraphBuilder
) {
    navGraphBuilder.addScreenWithTransitions(
        route = NavRoute.SettingScreenRoute.route,
    ) { backStackEntry ->
        InScreenBottomBarHost(destination = backStackEntry.destination) {
            SettingScreen(navController = navController)
        }
    }
}

private fun addLanguageScreen(
    navController: NavHostController, navGraphBuilder: NavGraphBuilder
) {
    navGraphBuilder.addScreenWithTransitions(
        route = NavRoute.LanguageScreenRoute.route
    ) {
        LocalizeScreen(
            navController = navController,
            isFirstSession = false,
        )
    }
}

private fun addProScreen(
    navController: NavHostController, navGraphBuilder: NavGraphBuilder
) {
    navGraphBuilder.addScreenWithTransitions(
        route = NavRoute.ProScreenRoute.route
    ) {
        ProScreen(navController = navController)
    }
}

private fun addCameraScreen(
    navController: NavHostController, navGraphBuilder: NavGraphBuilder
) {
    navGraphBuilder.addScreenWithTransitions(
        route = NavRoute.CameraScreenRoute.route,
        arguments = listOf(navArgument(NavArgs.POSE_ID) { type = NavType.IntType }),
    ) { backStackEntry ->
        PoseCameraScreen(
            navController = navController,
            poseId = backStackEntry.arguments?.getInt(NavArgs.POSE_ID)
                ?.takeIf { it != NavRoute.CameraScreenRoute.NO_POSE_ID },
        )
    }
}

private fun addPoseAlbumScreen(
    navController: NavHostController, navGraphBuilder: NavGraphBuilder
) {
    navGraphBuilder.addScreenWithTransitions(
        route = NavRoute.PoseAlbumScreenRoute.route,
        arguments = listOf(navArgument(NavArgs.CATEGORY) { type = NavType.StringType }),
    ) { backStackEntry ->
        PoseAlbumScreen(
            navController = navController,
            category = backStackEntry.arguments?.getString(NavArgs.CATEGORY).orEmpty(),
        )
    }
}

private fun addPoseDetailScreen(
    navController: NavHostController, navGraphBuilder: NavGraphBuilder
) {
    navGraphBuilder.addScreenWithTransitions(
        route = NavRoute.PoseDetailScreenRoute.route,
        arguments = listOf(navArgument(NavArgs.POSE_ID) { type = NavType.IntType }),
    ) { backStackEntry ->
        PoseDetailScreen(
            navController = navController,
            poseId = backStackEntry.arguments?.getInt(NavArgs.POSE_ID) ?: 0,
        )
    }
}

private fun addCaptureAlbumScreen(
    navController: NavHostController, navGraphBuilder: NavGraphBuilder
) {
    navGraphBuilder.addScreenWithTransitions(
        route = NavRoute.CaptureAlbumScreenRoute.route,
        arguments = listOf(navArgument(NavArgs.LOCATION_LABEL) { type = NavType.StringType }),
    ) { backStackEntry ->
        CaptureAlbumScreen(
            navController = navController,
            locationLabel = backStackEntry.arguments?.getString(NavArgs.LOCATION_LABEL).orEmpty(),
        )
    }
}

private fun addCaptureDetailScreen(
    navController: NavHostController, navGraphBuilder: NavGraphBuilder
) {
    navGraphBuilder.addScreenWithTransitions(
        route = NavRoute.CaptureDetailScreenRoute.route,
        arguments = listOf(navArgument(NavArgs.CAPTURE_ID) { type = NavType.LongType }),
    ) { backStackEntry ->
        CaptureDetailScreen(
            navController = navController,
            captureId = backStackEntry.arguments?.getLong(NavArgs.CAPTURE_ID) ?: 0L,
        )
    }
}

private fun addPhotoEditScreen(
    navController: NavHostController, navGraphBuilder: NavGraphBuilder
) {
    navGraphBuilder.addScreenWithTransitions(
        route = NavRoute.PhotoEditScreenRoute.route
    ) {
        PhotoEditScreen(navController = navController)
    }
}

private fun addPhotoSuccessScreen(
    navController: NavHostController, navGraphBuilder: NavGraphBuilder
) {
    navGraphBuilder.addScreenWithTransitions(
        route = NavRoute.PhotoSuccessScreenRoute.route,
        arguments = listOf(navArgument(NavArgs.CAPTURE_ID) { type = NavType.LongType }),
    ) { backStackEntry ->
        PhotoSuccessScreen(
            navController = navController,
            captureId = backStackEntry.arguments?.getLong(NavArgs.CAPTURE_ID) ?: 0L,
        )
    }
}

val bottomBarRoutes =
    setOf(
        NavRoute.HomeScreenRoute.route,
        NavRoute.CollectionsScreenRoute.route,
        NavRoute.SavedScreenRoute.route,
        NavRoute.SettingScreenRoute.route,
    )
