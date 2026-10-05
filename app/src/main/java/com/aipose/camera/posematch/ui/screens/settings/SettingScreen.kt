package com.aipose.camera.posematch.ui.screens.settings

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.aipose.camera.posematch.BuildConfig
import com.aipose.camera.posematch.R
import com.aipose.camera.posematch.ui.common.CaptureTimerSheet
import com.aipose.camera.posematch.ui.common.PoseGlowBackground
import com.aipose.camera.posematch.ui.common.PoseGlows
import com.aipose.camera.posematch.ui.common.adaptiveWidth
import com.aipose.camera.posematch.ui.graph.NavRoute
import com.aipose.camera.posematch.ui.graph.acceptNavigationClick
import com.aipose.camera.posematch.ui.graph.navigateOnClick
import com.aipose.camera.posematch.ui.models.CaptureTimer
import com.aipose.camera.posematch.ui.models.GlossyBadgePalette
import com.aipose.camera.posematch.ui.models.allLanguageItems
import com.aipose.camera.posematch.ui.models.themeOptionLabel
import com.aipose.camera.posematch.ui.screens.settings.components.SettingRow
import com.aipose.camera.posematch.ui.screens.settings.components.SettingSectionLabel
import com.aipose.camera.posematch.ui.screens.settings.components.SettingToggle
import com.aipose.camera.posematch.ui.screens.settings.components.SettingsFooter
import com.aipose.camera.posematch.ui.screens.settings.components.SettingsHeader
import com.aipose.camera.posematch.ui.screens.settings.components.SettingsStreakCard
import com.aipose.camera.posematch.ui.vm.CameraSettingsViewModel
import com.aipose.camera.posematch.ui.vm.LanguageViewModel
import com.aipose.camera.posematch.ui.vm.SettingsViewModel
import com.aipose.camera.posematch.ui.vm.ThemeViewModel
import com.aipose.camera.posematch.util.PRIVACY_POLICY
import com.aipose.camera.posematch.util.bidiIsolate
import com.aipose.camera.posematch.util.shareApp
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject

private const val TEXT_MIME_TYPE = "text/plain"

@Composable
fun SettingScreen(
    navController: NavHostController,
    themeViewModel: ThemeViewModel = koinInject(),
    languageViewModel: LanguageViewModel = koinInject(),
    settingsViewModel: SettingsViewModel = koinViewModel(),
    cameraSettingsViewModel: CameraSettingsViewModel = koinViewModel(),
) {
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current
    val themeOption by themeViewModel.themeOption.collectAsStateWithLifecycle()
    val progress by settingsViewModel.progress.collectAsStateWithLifecycle()
    val retainSkeleton by cameraSettingsViewModel.retainSkeleton.collectAsStateWithLifecycle()
    val captureTimer by cameraSettingsViewModel.captureTimer.collectAsStateWithLifecycle()
    var isTimerSheetVisible by rememberSaveable { mutableStateOf(false) }
    val shareChooserTitle = stringResource(R.string.app_name)
    val languageName = allLanguageItems
        .firstOrNull { item -> item.code == languageViewModel.currentLanguageCode }
        ?.name
        ?: stringResource(R.string.settings_language_value)

    PoseGlowBackground(glows = PoseGlows.Settings) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .adaptiveWidth()
                .statusBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(start = 20.dp, end = 20.dp, top = 22.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            SettingsHeader()
            SettingsStreakCard(
                title = pluralStringResource(R.plurals.settings_streak_title, progress.dayStreak, progress.dayStreak),
                subtitle = stringResource(
                    R.string.settings_streak_subtitle,
                    progress.shotsTaken,
                    progress.averageMatch,
                ),
                onClick = { navController.navigateOnClick(NavRoute.AchievementsScreenRoute.route) },
                modifier = Modifier.padding(top = 7.dp),
            )
            SettingSectionLabel(
                text = stringResource(R.string.settings_preferences),
                modifier = Modifier.padding(top = 20.dp),
            )
            SettingRow(
                iconRes = R.drawable.ic_pose_globe,
                palette = GlossyBadgePalette.Violet,
                title = stringResource(R.string.settings_language),
                value = languageName,
                onClick = { navController.navigateOnClick(NavRoute.LanguageScreenRoute.route) },
            )
            SettingRow(
                iconRes = R.drawable.ic_pose_palette,
                palette = GlossyBadgePalette.Pink,
                title = stringResource(R.string.settings_theme),
                value = stringResource(themeOptionLabel(themeOption)),
                onClick = { navController.navigateOnClick(NavRoute.ThemePickerScreenRoute.route) },
            )
            SettingRow(
                iconRes = R.drawable.ic_pose_person,
                palette = GlossyBadgePalette.Cyan,
                title = stringResource(R.string.settings_retain_skeleton),
                onClick = { cameraSettingsViewModel.setRetainSkeleton(!retainSkeleton) },
                trailing = {
                    SettingToggle(
                        checked = retainSkeleton,
                        onCheckedChange = cameraSettingsViewModel::setRetainSkeleton,
                    )
                },
            )
            SettingRow(
                iconRes = R.drawable.ic_pose_timer,
                palette = GlossyBadgePalette.Amber,
                title = stringResource(R.string.settings_capture_timer),
                value = captureTimerLabel(captureTimer),
                onClick = { isTimerSheetVisible = true },
            )
            SettingSectionLabel(
                text = stringResource(R.string.settings_about),
                modifier = Modifier.padding(top = 20.dp),
            )
            SettingRow(
                iconRes = R.drawable.ic_pose_share,
                palette = GlossyBadgePalette.Emerald,
                title = stringResource(R.string.settings_share_app),
                onClick = {
                    if (navController.acceptNavigationClick()) {
                        val sendIntent = Intent(Intent.ACTION_SEND).apply {
                            type = TEXT_MIME_TYPE
                            putExtra(Intent.EXTRA_TEXT, context.shareApp())
                        }
                        context.startActivity(Intent.createChooser(sendIntent, shareChooserTitle))
                    }
                },
            )
            SettingRow(
                iconRes = R.drawable.ic_pose_shield,
                palette = GlossyBadgePalette.Blue,
                title = stringResource(R.string.settings_privacy_policy),
                onClick = {
                    if (PRIVACY_POLICY.isNotEmpty() && navController.acceptNavigationClick()) {
                        uriHandler.openUri(PRIVACY_POLICY)
                    }
                },
            )
            SettingsFooter(
                versionName = BuildConfig.VERSION_NAME,
                modifier = Modifier.padding(top = 16.dp),
            )
        }
    }

    if (isTimerSheetVisible) {
        CaptureTimerSheet(
            selected = captureTimer,
            onApply = { timer ->
                cameraSettingsViewModel.setCaptureTimer(timer)
                isTimerSheetVisible = false
            },
            onDismiss = { isTimerSheetVisible = false },
        )
    }
}

@Composable
private fun captureTimerLabel(timer: CaptureTimer): String =
    if (timer.isEnabled) {
        stringResource(R.string.settings_capture_timer_seconds, timer.seconds).bidiIsolate()
    } else {
        stringResource(R.string.settings_capture_timer_off)
    }
