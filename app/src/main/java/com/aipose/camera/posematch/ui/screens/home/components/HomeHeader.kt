package com.aipose.camera.posematch.ui.screens.home.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aipose.camera.posematch.R
import com.aipose.camera.posematch.ui.screens.pro.components.ProCrownBadge
import com.aipose.camera.posematch.ui.theme.PoseVioletLight
import com.aipose.camera.posematch.ui.theme.poseTextStyle

private val ProBadgeSize = 30.dp

@Composable
internal fun HomeHeader(
    showProBadge: Boolean,
    onProClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(end = 2.dp, bottom = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {

        val titleStyle = poseTextStyle(21.sp, FontWeight.Bold, Color.White)
        Text(
            modifier = Modifier.weight(1f),
            text = buildAnnotatedString {
                append(stringResource(R.string.home_title_pose))
                withStyle(titleStyle.toSpanStyle().copy(color = PoseVioletLight)) {
                    append(stringResource(R.string.home_title_ai))
                }
            },
            style = titleStyle,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )

        if (showProBadge) {
            Spacer(modifier = Modifier.width(8.dp))
            ProCrownBadge(
                size = ProBadgeSize,
                contentDescription = stringResource(R.string.premium_feature_go_premium_title),
                onClick = onProClick,
            )
        }
    }
}
