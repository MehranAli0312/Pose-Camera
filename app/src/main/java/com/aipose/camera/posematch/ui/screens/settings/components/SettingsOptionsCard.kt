package com.aipose.camera.posematch.ui.screens.settings.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.aipose.camera.posematch.R
import com.aipose.camera.posematch.ui.common.AppHorizontalDivider
import com.aipose.camera.posematch.ui.common.poseRaisedSurface
import com.aipose.camera.posematch.ui.models.GlossyBadgePalette

private val CardShape = RoundedCornerShape(20.dp)

@Composable
internal fun SettingsOptionsCard(
    languageName: String,
    onLanguageClick: () -> Unit,
    onRateUsClick: () -> Unit,
    onShareClick: () -> Unit,
    onPrivacyPolicyClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .poseRaisedSurface(CardShape),
    ) {
        SettingRow(
            iconRes = R.drawable.ic_pose_globe,
            palette = GlossyBadgePalette.Violet,
            title = stringResource(R.string.settings_language),
            value = languageName,
            onClick = onLanguageClick,
        )
        AppHorizontalDivider()
        SettingRow(
            iconRes = R.drawable.ic_pose_rate_us,
            palette = GlossyBadgePalette.Amber,
            title = stringResource(R.string.settings_rate_us),
            onClick = onRateUsClick,
        )
        AppHorizontalDivider()
        SettingRow(
            iconRes = R.drawable.ic_pose_share_app,
            palette = GlossyBadgePalette.Emerald,
            title = stringResource(R.string.settings_share_app),
            onClick = onShareClick,
        )
        AppHorizontalDivider()
        SettingRow(
            iconRes = R.drawable.ic_pose_shield,
            palette = GlossyBadgePalette.Blue,
            title = stringResource(R.string.settings_privacy_policy),
            onClick = onPrivacyPolicyClick,
        )
    }
}
