package com.aipose.camera.posematch.ui.graph

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.aipose.camera.posematch.ui.common.addScreenWithTransitions
import com.aipose.camera.posematch.ui.screens.achievements.AchievementsScreen
import com.aipose.camera.posematch.ui.screens.camera.PoseCameraScreen
import com.aipose.camera.posematch.ui.screens.captureAlbum.CaptureAlbumScreen
import com.aipose.camera.posematch.ui.screens.captureDetail.CaptureDetailScreen
import com.aipose.camera.posematch.ui.screens.collections.CollectionsScreen
import com.aipose.camera.posematch.ui.screens.home.HomeScreen
import com.aipose.camera.posematch.ui.screens.language.LocalizeScreen
import com.aipose.camera.posematch.ui.screens.photoEdit.PhotoEditScreen
import com.aipose.camera.posematch.ui.screens.photoSuccess.PhotoSuccessScreen
import com.aipose.camera.posematch.ui.screens.poseAlbum.PoseAlbumScreen
import com.aipose.camera.posematch.ui.screens.pro.PremiumScreen
import com.aipose.camera.posematch.ui.screens.progress.ProgressScreen
import com.aipose.camera.posematch.ui.screens.settings.SettingScreen

fun NavGraphBuilder.addMainScreens(navController: NavHostController) {
    addHomeScreen(navController)
    addCollectionsScreen(navController)
    addSettingScreen(navController)
    addLanguageScreen(navController)
    addProScreen(navController)
    addCameraScreen(navController)
    addPoseAlbumScreen(navController)
    addCaptureAlbumScreen(navController)
    addCaptureDetailScreen(navController)
    addPhotoEditScreen(navController)
    addProgressScreen(navController)
    addAchievementsScreen(navController)
    addPhotoSuccessScreen(navController)
}

private fun NavGraphBuilder.addHomeScreen(navController: NavHostController) {
    addScreenWithTransitions(route = NavRoute.HomeScreenRoute.route) {
        HomeScreen(navController = navController)
    }
}

private fun NavGraphBuilder.addCollectionsScreen(navController: NavHostController) {
    addScreenWithTransitions(route = NavRoute.CollectionsScreenRoute.route) {
        CollectionsScreen(navController = navController)
    }
}

private fun NavGraphBuilder.addSettingScreen(navController: NavHostController) {
    addScreenWithTransitions(route = NavRoute.SettingScreenRoute.route) {
        SettingScreen(navController = navController)
    }
}

private fun NavGraphBuilder.addLanguageScreen(navController: NavHostController) {
    addScreenWithTransitions(route = NavRoute.LanguageScreenRoute.route) {
        LocalizeScreen(
            navController = navController,
            isFirstSession = false,
        )
    }
}

private fun NavGraphBuilder.addProScreen(navController: NavHostController) {
    addScreenWithTransitions(route = NavRoute.ProScreenRoute.route) {
        PremiumScreen(navController = navController)
    }
}

private fun NavGraphBuilder.addCameraScreen(navController: NavHostController) {
    addScreenWithTransitions(
        route = NavRoute.CameraScreenRoute.route,
        arguments = listOf(navArgument(NavArgs.POSE_ID) { type = NavType.IntType }),
    ) { backStackEntry ->
        PoseCameraScreen(
            navController = navController,
            poseId = backStackEntry.arguments?.getInt(NavArgs.POSE_ID)
                ?.takeIf { poseId -> poseId != NavRoute.CameraScreenRoute.NO_POSE_ID },
        )
    }
}

private fun NavGraphBuilder.addPoseAlbumScreen(navController: NavHostController) {
    addScreenWithTransitions(
        route = NavRoute.PoseAlbumScreenRoute.route,
        arguments = listOf(navArgument(NavArgs.CATEGORY) { type = NavType.StringType }),
    ) { backStackEntry ->
        PoseAlbumScreen(
            navController = navController,
            category = backStackEntry.arguments?.getString(NavArgs.CATEGORY).orEmpty(),
        )
    }
}

private fun NavGraphBuilder.addCaptureAlbumScreen(navController: NavHostController) {
    addScreenWithTransitions(
        route = NavRoute.CaptureAlbumScreenRoute.route,
        arguments = listOf(navArgument(NavArgs.LOCATION_LABEL) { type = NavType.StringType }),
    ) { backStackEntry ->
        CaptureAlbumScreen(
            navController = navController,
            locationLabel = backStackEntry.arguments?.getString(NavArgs.LOCATION_LABEL).orEmpty(),
        )
    }
}

private fun NavGraphBuilder.addCaptureDetailScreen(navController: NavHostController) {
    addScreenWithTransitions(
        route = NavRoute.CaptureDetailScreenRoute.route,
        arguments = listOf(navArgument(NavArgs.CAPTURE_ID) { type = NavType.LongType }),
    ) { backStackEntry ->
        CaptureDetailScreen(
            navController = navController,
            captureId = backStackEntry.arguments?.getLong(NavArgs.CAPTURE_ID) ?: 0L,
        )
    }
}

private fun NavGraphBuilder.addPhotoEditScreen(navController: NavHostController) {
    addScreenWithTransitions(route = NavRoute.PhotoEditScreenRoute.route) {
        PhotoEditScreen(navController = navController)
    }
}

private fun NavGraphBuilder.addPhotoSuccessScreen(navController: NavHostController) {
    addScreenWithTransitions(
        route = NavRoute.PhotoSuccessScreenRoute.route,
        arguments = listOf(navArgument(NavArgs.CAPTURE_ID) { type = NavType.LongType }),
    ) { backStackEntry ->
        PhotoSuccessScreen(
            navController = navController,
            captureId = backStackEntry.arguments?.getLong(NavArgs.CAPTURE_ID) ?: 0L,
        )
    }
}

private fun NavGraphBuilder.addProgressScreen(navController: NavHostController) {
    addScreenWithTransitions(route = NavRoute.ProgressScreenRoute.route) {
        ProgressScreen(navController = navController)
    }
}

private fun NavGraphBuilder.addAchievementsScreen(navController: NavHostController) {
    addScreenWithTransitions(route = NavRoute.AchievementsScreenRoute.route) {
        AchievementsScreen(navController = navController)
    }
}
