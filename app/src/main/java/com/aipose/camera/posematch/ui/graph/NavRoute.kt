package com.aipose.camera.posematch.ui.graph

import android.net.Uri
import com.aipose.camera.posematch.ui.screens.launch.models.LaunchFlow

private object Routes {

    const val SPLASH_SCREEN = "SPLASH_SCREEN"
    const val LANGUAGE_SCREEN = "LANGUAGE_SCREEN"
    const val ONBOARD_SCREEN = "ONBOARD_SCREEN"
    const val HOME_SCREEN = "HOME_SCREEN"
    const val COLLECTIONS_SCREEN = "COLLECTIONS_SCREEN"
    const val SETTING_SCREEN = "SETTING_SCREEN"
    const val PRO_SCREEN = "UPGRADE_SCREEN"
    const val START_PREMIUM_SCREEN = "START_PREMIUM_SCREEN"
    const val CAMERA_SCREEN = "CAMERA_SCREEN"
    const val POSE_ALBUM_SCREEN = "POSE_ALBUM_SCREEN"
    const val CAPTURE_ALBUM_SCREEN = "CAPTURE_ALBUM_SCREEN"
    const val CAPTURE_DETAIL_SCREEN = "CAPTURE_DETAIL_SCREEN"
    const val PHOTO_EDIT_SCREEN = "PHOTO_EDIT_SCREEN"
    const val PHOTO_SUCCESS_SCREEN = "PHOTO_SUCCESS_SCREEN"
    const val PROGRESS_SCREEN = "PROGRESS_SCREEN"
    const val ACHIEVEMENTS_SCREEN = "ACHIEVEMENTS_SCREEN"
}

object NavArgs {
    const val POSE_ID = "poseId"
    const val CATEGORY = "category"
    const val LOCATION_LABEL = "locationLabel"
    const val CAPTURE_ID = "captureId"
    const val LAUNCH_FLOW = "launchFlow"
}

sealed class NavRoute(val route: String) {
    data object SplashScreenRoute : NavRoute(Routes.SPLASH_SCREEN)
    data object LanguageScreenRoute : NavRoute(Routes.LANGUAGE_SCREEN) {
        val launchRoute: String = route + "/{" + NavArgs.LAUNCH_FLOW + "}"
        fun routeFor(launchFlow: LaunchFlow): String =
            route + "/" + Uri.encode(launchFlow.encode())
    }
    data object OnboardScreenRoute : NavRoute(
        Routes.ONBOARD_SCREEN + "/{" + NavArgs.LAUNCH_FLOW + "}",
    ) {
        fun routeFor(launchFlow: LaunchFlow): String =
            Routes.ONBOARD_SCREEN + "/" + Uri.encode(launchFlow.encode())
    }
    data object HomeScreenRoute : NavRoute(Routes.HOME_SCREEN)
    data object CollectionsScreenRoute : NavRoute(Routes.COLLECTIONS_SCREEN)
    data object SettingScreenRoute : NavRoute(Routes.SETTING_SCREEN)
    data object ProScreenRoute : NavRoute(Routes.PRO_SCREEN)
    data object PhotoEditScreenRoute : NavRoute(Routes.PHOTO_EDIT_SCREEN)
    data object ProgressScreenRoute : NavRoute(Routes.PROGRESS_SCREEN)
    data object AchievementsScreenRoute : NavRoute(Routes.ACHIEVEMENTS_SCREEN)

    data object StartPremiumScreenRoute :
        NavRoute(Routes.START_PREMIUM_SCREEN + "/{" + NavArgs.LAUNCH_FLOW + "}") {
        fun routeFor(launchFlow: LaunchFlow): String =
            Routes.START_PREMIUM_SCREEN + "/" + Uri.encode(launchFlow.encode())
    }

    data object CameraScreenRoute :
        NavRoute(Routes.CAMERA_SCREEN + "/{" + NavArgs.POSE_ID + "}") {
        const val NO_POSE_ID = 0

        fun routeFor(poseId: Int): String = Routes.CAMERA_SCREEN + "/" + poseId

        fun routeWithoutPose(): String = routeFor(NO_POSE_ID)
    }

    data object PoseAlbumScreenRoute :
        NavRoute(Routes.POSE_ALBUM_SCREEN + "/{" + NavArgs.CATEGORY + "}") {
        const val ALL_CATEGORIES = "__all__"

        fun routeFor(category: String): String =
            Routes.POSE_ALBUM_SCREEN + "/" + Uri.encode(category)
    }

    data object CaptureAlbumScreenRoute :
        NavRoute(Routes.CAPTURE_ALBUM_SCREEN + "/{" + NavArgs.LOCATION_LABEL + "}") {
        fun routeFor(locationLabel: String): String =
            Routes.CAPTURE_ALBUM_SCREEN + "/" + Uri.encode(locationLabel)
    }

    data object CaptureDetailScreenRoute :
        NavRoute(Routes.CAPTURE_DETAIL_SCREEN + "/{" + NavArgs.CAPTURE_ID + "}") {
        fun routeFor(captureId: Long): String = Routes.CAPTURE_DETAIL_SCREEN + "/" + captureId
    }

    data object PhotoSuccessScreenRoute :
        NavRoute(Routes.PHOTO_SUCCESS_SCREEN + "/{" + NavArgs.CAPTURE_ID + "}") {
        fun routeFor(captureId: Long): String = Routes.PHOTO_SUCCESS_SCREEN + "/" + captureId
    }
}
