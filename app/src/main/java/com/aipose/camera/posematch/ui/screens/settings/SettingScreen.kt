package com.aipose.camera.posematch.ui.screens.settings

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.aipose.camera.posematch.R
import com.aipose.camera.posematch.ui.common.PoseGlowBackground
import com.aipose.camera.posematch.ui.common.PoseGlows
import com.aipose.camera.posematch.ui.common.PoseScreenGutter
import com.aipose.camera.posematch.ui.common.RateUsDialog
import com.aipose.camera.posematch.ui.common.adaptiveWidth
import com.aipose.camera.posematch.ui.common.poseScreenPadding
import com.aipose.camera.posematch.ui.graph.NavRoute
import com.aipose.camera.posematch.ui.graph.acceptNavigationClick
import com.aipose.camera.posematch.ui.graph.navigateOnClick
import com.aipose.camera.posematch.ui.graph.popBackStackOnClick
import com.aipose.camera.posematch.ui.models.allLanguageItems
import com.aipose.camera.posematch.ui.screens.settings.components.SettingsOptionsCard
import com.aipose.camera.posematch.ui.screens.settings.components.SettingsProCard
import com.aipose.camera.posematch.ui.screens.settings.components.SettingsHeader
import com.aipose.camera.posematch.ui.vm.LanguageViewModel
import com.aipose.camera.posematch.ui.vm.SettingsViewModel
import com.aipose.camera.posematch.util.PRIVACY_POLICY
import com.aipose.camera.posematch.util.shareApp
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject

private const val TEXT_MIME_TYPE = "text/plain"

@Composable
fun SettingScreen(
    navController: NavHostController,
    languageViewModel: LanguageViewModel = koinInject(),
    settingsViewModel: SettingsViewModel = koinViewModel(),
) {
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current
    val showProCard = settingsViewModel.showProCard.collectAsStateWithLifecycle().value
    val isRateUsVisible = rememberSaveable { mutableStateOf(false) }
    val shareChooserTitle = stringResource(R.string.app_name)
    val languageName = allLanguageItems
        .firstOrNull { item -> item.code == languageViewModel.currentLanguageCode }
        ?.name
        ?: stringResource(R.string.settings_language_value)

    PoseGlowBackground(glows = PoseGlows.Settings) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .adaptiveWidth(),
        ) {
            SettingsHeader(
                onBack = navController::popBackStackOnClick,
                modifier = Modifier.poseScreenPadding(),
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(
                        start = PoseScreenGutter,
                        end = PoseScreenGutter,
                        top = 10.dp,
                        bottom = 24.dp,
                    ),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                if (showProCard) {
                    SettingsProCard(
                        onClick = { navController.navigateOnClick(NavRoute.ProScreenRoute.route) },
                        modifier = Modifier.padding(top = 10.dp),
                    )
                }
                SettingsOptionsCard(
                    languageName = languageName,
                    onLanguageClick = {
                        navController.navigateOnClick(NavRoute.LanguageScreenRoute.route)
                    },
                    onRateUsClick = { isRateUsVisible.value = true },
                    onShareClick = {
                        if (navController.acceptNavigationClick()) {
                            val sendIntent = Intent(Intent.ACTION_SEND).apply {
                                type = TEXT_MIME_TYPE
                                putExtra(Intent.EXTRA_TEXT, context.shareApp())
                            }
                            context.startActivity(Intent.createChooser(sendIntent, shareChooserTitle))
                        }
                    },
                    onPrivacyPolicyClick = {
                        if (PRIVACY_POLICY.isNotEmpty() && navController.acceptNavigationClick()) {
                            uriHandler.openUri(PRIVACY_POLICY)
                        }
                    },
                    modifier = Modifier.padding(top = 10.dp),
                )
            }
        }
    }

    RateUsDialog(visible = isRateUsVisible)
}
