package com.aipose.camera.posematch.ui.screens.themePicker

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.aipose.camera.posematch.R
import com.aipose.camera.posematch.domain.models.AppThemeOption
import com.aipose.camera.posematch.ui.common.PoseCtaButton
import com.aipose.camera.posematch.ui.common.PoseGlowBackground
import com.aipose.camera.posematch.ui.common.PoseGlows
import com.aipose.camera.posematch.ui.common.PoseRaisedIconButton
import com.aipose.camera.posematch.ui.common.adaptiveWidth
import com.aipose.camera.posematch.ui.common.click
import com.aipose.camera.posematch.ui.graph.popBackStackOnClick
import com.aipose.camera.posematch.ui.screens.themePicker.components.ThemeOptionCard
import com.aipose.camera.posematch.ui.theme.LocalAppPalette
import com.aipose.camera.posematch.ui.theme.poseTextStyle
import com.aipose.camera.posematch.ui.vm.ThemeViewModel
import org.koin.compose.koinInject

private val BackGlyphSize = DpSize(10.dp, 17.dp)

@Composable
fun ThemePickerScreen(
    navController: NavHostController,
    themeViewModel: ThemeViewModel = koinInject(),
) {
    val currentOption by themeViewModel.themeOption.collectAsStateWithLifecycle()
    var selectedKey by rememberSaveable(currentOption) { mutableStateOf(currentOption.key) }
    val selected = AppThemeOption.fromKey(selectedKey)
    val muted = LocalAppPalette.current.textMuted

    PoseGlowBackground(glows = PoseGlows.ThemePicker) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .adaptiveWidth()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp),
        ) {
            Row(
                modifier = Modifier.padding(top = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                PoseRaisedIconButton(
                    iconRes = R.drawable.ic_pose_back,
                    contentDescription = stringResource(R.string.action_back),
                    glyphSize = BackGlyphSize,
                    onClick = navController::popBackStackOnClick,
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = stringResource(R.string.settings_theme),
                    style = poseTextStyle(22.sp, FontWeight.Bold, Color.White),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Text(
                text = stringResource(R.string.theme_picker_subtitle),
                style = poseTextStyle(12.5.sp, FontWeight.Normal, muted),
                modifier = Modifier.padding(top = 18.dp),
            )
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(top = 22.dp, bottom = 20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(AppThemeOption.entries, key = { option -> option.key }) { option ->
                    ThemeOptionCard(
                        option = option,
                        isSelected = option == selected,
                        isCurrent = option == currentOption,
                        onClick = { selectedKey = option.key },
                    )
                }
            }
            PoseCtaButton(
                text = stringResource(R.string.theme_apply),
                onClick = {
                    themeViewModel.updateTheme(selected)
                    navController.popBackStackOnClick()
                },
            )
            Text(
                text = stringResource(R.string.action_cancel),
                style = poseTextStyle(12.sp, FontWeight.Bold, muted),
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(top = 12.dp, bottom = 8.dp)
                    .click(onClick = navController::popBackStackOnClick)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
            )
            Spacer(modifier = Modifier.height(4.dp))
        }
    }
}
