package com.aipose.camera.posematch.ui.screens.saved.components

import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aipose.camera.posematch.R
import com.aipose.camera.posematch.ui.common.bounceClick
import com.aipose.camera.posematch.ui.theme.LocalAppPalette
import com.aipose.camera.posematch.ui.theme.PoseVioletLight
import com.aipose.camera.posematch.ui.theme.poseTextStyle

@Composable
internal fun SavedSectionHeader(
    @StringRes titleRes: Int,
    modifier: Modifier = Modifier,
    onSeeAll: (() -> Unit)? = null,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(titleRes),
            style = poseTextStyle(9.sp, FontWeight.Bold, LocalAppPalette.current.textFaint),
            modifier = Modifier.weight(1f),
        )
        if (onSeeAll != null) {
            Row(
                modifier = Modifier.bounceClick(onClick = onSeeAll),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(7.dp),
            ) {
                Text(
                    text = stringResource(R.string.saved_see_all),
                    style = poseTextStyle(11.sp, FontWeight.Bold, PoseVioletLight),
                )
                Image(
                    painter = painterResource(R.drawable.ic_pose_chevron_end),
                    contentDescription = null,
                    colorFilter = ColorFilter.tint(PoseVioletLight),
                    modifier = Modifier.size(width = 8.dp, height = 10.dp),
                )
            }
        }
    }
}
