package com.aipose.camera.posematch.ui.screens.settings

import android.annotation.SuppressLint
import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.common.showToast
import com.aipose.camera.posematch.R
import com.aipose.camera.posematch.domain.models.RateUsOutcome
import com.aipose.camera.posematch.ui.common.AppBar
import com.aipose.camera.posematch.ui.common.ProAppBarAction
import com.aipose.camera.posematch.ui.common.getActivity
import com.aipose.camera.posematch.ui.graph.NavRoute
import com.aipose.camera.posematch.ui.graph.acceptNavigationClick
import com.aipose.camera.posematch.ui.graph.navigateOnClick
import com.aipose.camera.posematch.ui.screens.bottomSheet.ExitPopUp
import com.aipose.camera.posematch.ui.screens.dialog.PremiumRateUsDialog
import com.aipose.camera.posematch.ui.screens.settings.components.SettingDivider
import com.aipose.camera.posematch.ui.screens.settings.components.SettingGroupCard
import com.aipose.camera.posematch.ui.screens.settings.components.SettingItem
import com.aipose.camera.posematch.ui.screens.settings.components.SettingSectionLabel
import com.aipose.camera.posematch.ui.screens.settings.components.ThemeModeSelector
import com.aipose.camera.posematch.ui.screens.settings.components.SettingToggleItem
import com.aipose.camera.posematch.ui.vm.CameraSettingsViewModel
import com.aipose.camera.posematch.ui.vm.RateUsViewModel
import com.aipose.camera.posematch.ui.vm.ThemeViewModel
import com.aipose.camera.posematch.util.PRIVACY_POLICY
import com.aipose.camera.posematch.util.contact
import com.aipose.camera.posematch.util.getAppLink
import com.aipose.camera.posematch.util.shareApp
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject

@SuppressLint("LocalContextResourcesRead")
@Composable
fun SettingScreen(
    navController: NavHostController,
    themeViewModel: ThemeViewModel = koinInject(),
    rateUsViewModel: RateUsViewModel = koinInject(),
    cameraSettingsViewModel: CameraSettingsViewModel = koinViewModel(),
) {
    val context = LocalContext.current
    val activity = getActivity()
    val uriHandler = LocalUriHandler.current
    val themeOption by themeViewModel.themeOption.collectAsStateWithLifecycle()
    val retainSkeleton by cameraSettingsViewModel.retainSkeleton.collectAsStateWithLifecycle()

    val sendIntent: Intent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(
            Intent.EXTRA_TEXT, context.shareApp().trimIndent()
        )
        type = "text/plain"
    }

    val shareIntent = Intent.createChooser(sendIntent, stringResource(R.string.app_name))

    val showExitDialog = rememberSaveable {
        mutableStateOf(false)
    }

    ExitPopUp(showExitDialog) {
        showExitDialog.value = false
        activity?.finishAffinity()
    }

    val rateUsDialog = rememberSaveable {
        mutableStateOf(false)
    }
    var rating by remember { mutableFloatStateOf(5f) }

    PremiumRateUsDialog(maxStars = 5, rating = rating, rateUsDialog, onRatingChanged = {
        rating = it
    }, onRatingCallback = {
        when (rateUsViewModel.submitRating(rating)) {
            RateUsOutcome.OpenStore -> {
                val appLink = context.getAppLink()
                if (appLink.isNotEmpty() && navController.acceptNavigationClick()) {
                    rateUsDialog.value = false
                    uriHandler.openUri(appLink)
                }
            }

            RateUsOutcome.SendFeedback -> {
                if (navController.acceptNavigationClick()) {
                    rateUsDialog.value = false
                    context.contact()
                }
            }

            RateUsOutcome.NotRated -> {
                context.showToast(context.resources.getString(R.string.please_rate))
            }
        }
    })

    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {
        AppBar(
            text = stringResource(R.string.item_settings),
            menuItems = {
                ProAppBarAction {
                    navController.navigateOnClick(NavRoute.ProScreenRoute.route)
                }
            },
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {

            SettingGroupCard {
                ThemeModeSelector(
                    selectedOption = themeOption,
                    onThemeSelected = { option -> themeViewModel.updateTheme(option) },
                )
            }

            SettingSectionLabel(text = stringResource(R.string.settings_section_camera))

            SettingGroupCard {
                SettingToggleItem(
                    iconRes = R.drawable.ic_skeleton_overlay,
                    title = stringResource(R.string.settings_retain_skeleton),
                    subtitle = stringResource(R.string.settings_retain_skeleton_desc),
                    checked = retainSkeleton,
                    onCheckedChange = cameraSettingsViewModel::setRetainSkeleton,
                )
            }

            SettingSectionLabel(text = stringResource(R.string.settings_more))

            SettingGroupCard {
                SettingItem(
                    iconRes = R.drawable.ic_language,
                    title = stringResource(R.string.settings_language),
                    subtitle = stringResource(R.string.settings_language_value),
                    showChevron = true,
                    onClick = {
                        navController.navigateOnClick(NavRoute.LanguageScreenRoute.route)
                    },
                )
                SettingDivider()
                SettingItem(
                    iconRes = R.drawable.ic_rate_us,
                    title = stringResource(R.string.settings_rate_us),
                    subtitle = stringResource(R.string.settings_rate_us_desc),
                    showChevron = true,
                    onClick = {
                        rateUsDialog.value = true
                    },
                )
                SettingDivider()
                SettingItem(
                    iconRes = R.drawable.ic_privacy_policy,
                    title = stringResource(R.string.settings_privacy_policy),
                    subtitle = stringResource(R.string.settings_privacy_policy_desc),
                    showChevron = true,
                    onClick = {
                        if (PRIVACY_POLICY.isNotEmpty() && navController.acceptNavigationClick()) {
                            uriHandler.openUri(PRIVACY_POLICY)
                        }
                    },
                )
                SettingDivider()
                SettingItem(
                    iconRes = R.drawable.ic_share_app,
                    title = stringResource(R.string.settings_share_app),
                    subtitle = stringResource(R.string.settings_share_app_desc),
                    showChevron = true,
                    onClick = {
                        val shareApp = context.shareApp()
                        if (shareApp.isNotEmpty() && navController.acceptNavigationClick()) {
                            context.startActivity(shareIntent)
                        }
                    },
                )
                SettingDivider()
                SettingItem(
                    iconRes = R.drawable.ic_exit,
                    title = stringResource(R.string.settings_exit),
                    subtitle = stringResource(R.string.settings_exit_desc),
                    showChevron = true,
                    onClick = {
                        showExitDialog.value = true
                    },
                )
            }
        }
    }
}
