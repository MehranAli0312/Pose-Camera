package com.aipose.camera.posematch.ui.screens.onboard.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aipose.camera.posematch.ui.common.LtrLayout
import com.aipose.camera.posematch.ui.screens.onboard.data.ONBOARD_POSE_COUNT
import com.aipose.camera.posematch.ui.screens.onboard.models.OnboardStage
import com.aipose.camera.posematch.ui.screens.onboard.models.OnboardStep
import com.aipose.camera.posematch.ui.theme.PoseTextMuted
import com.aipose.camera.posematch.ui.theme.poseTextStyle

private val PanelToTitle = 30.dp
private val TitleToDescription = 12.dp
private val TitleSize = 27.sp
private val DescriptionSize = 13.sp
private val DescriptionPadding = 16.dp
private val DescriptionLineHeight = 19.sp

@Composable
internal fun OnboardContentPage(
    step: OnboardStep,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        OnboardStagePanel(modifier = Modifier.padding(horizontal = 20.dp)) {
            LtrLayout {
                when (step.stage) {
                    OnboardStage.PickPose -> OnboardPoseStage()
                    OnboardStage.MatchOutline -> OnboardMatchStage()
                    OnboardStage.SnapSave -> OnboardSaveStage()
                }
            }
        }
        Spacer(modifier = Modifier.height(PanelToTitle))
        Text(
            text = stringResource(step.titleRes),
            style = poseTextStyle(TitleSize, FontWeight.Bold, Color.White),
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(modifier = Modifier.height(TitleToDescription))
        Text(
            text = descriptionFor(step),
            style = poseTextStyle(DescriptionSize, FontWeight.Normal, PoseTextMuted)
                .copy(lineHeight = DescriptionLineHeight),
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = DescriptionPadding),
        )
    }
}

@Composable
private fun descriptionFor(step: OnboardStep): String = when (step.stage) {
    OnboardStage.PickPose -> stringResource(step.descriptionRes, ONBOARD_POSE_COUNT)
    else -> stringResource(step.descriptionRes)
}
