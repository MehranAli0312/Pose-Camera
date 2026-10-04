package com.aipose.camera.posematch.ui.screens.photoSuccess

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.aipose.camera.posematch.R
import com.aipose.camera.posematch.ui.common.PoseGlowBackground
import com.aipose.camera.posematch.ui.common.adaptiveWidth
import com.aipose.camera.posematch.ui.common.shareImageFile
import com.aipose.camera.posematch.ui.common.PoseGlows
import com.aipose.camera.posematch.ui.graph.NavRoute
import com.aipose.camera.posematch.ui.graph.navigateToTab
import com.aipose.camera.posematch.ui.graph.popBackStackOnClick
import com.aipose.camera.posematch.ui.screens.photoSuccess.components.PhotoSuccessActions
import com.aipose.camera.posematch.ui.screens.photoSuccess.components.PhotoSuccessHeader
import com.aipose.camera.posematch.ui.screens.photoSuccess.components.PhotoSuccessShot
import com.aipose.camera.posematch.ui.screens.photoSuccess.models.PhotoSuccessUiState
import com.aipose.camera.posematch.ui.vm.PhotoSuccessViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun PhotoSuccessScreen(
    navController: NavHostController,
    captureId: Long,
    viewModel: PhotoSuccessViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val shareChooserTitle = stringResource(R.string.share_frame)

    LaunchedEffect(captureId) { viewModel.onCaptureRequested(captureId) }

    val content = uiState as? PhotoSuccessUiState.Content

    PoseGlowBackground(glows = PoseGlows.CaptureSaved) {
        if (content == null) return@PoseGlowBackground
        val capture = content.capture
        val title = capture.title.ifBlank { stringResource(R.string.camera_no_pose) }
        val place = capture.location.name ?: stringResource(R.string.unknown_location)
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .adaptiveWidth()
                .statusBarsPadding()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.height(62.dp))
            PhotoSuccessHeader(
                subtitle = if (content.isPersonalBest) {
                    stringResource(R.string.saved_subtitle_best, title)
                } else {
                    stringResource(R.string.saved_subtitle, title)
                },
            )
            Spacer(modifier = Modifier.height(28.dp))
            PhotoSuccessShot(
                imagePath = capture.imagePath,
                matchScore = capture.matchScore.takeIf { capture.poseId != null && it > 0 },
                placeLabel = stringResource(R.string.saved_place_now, place),
            )
            Spacer(modifier = Modifier.height(30.dp))
            PhotoSuccessActions(
                onShare = { context.shareImageFile(capture.imagePath, shareChooserTitle) },
                onShootAgain = navController::popBackStackOnClick,
                onViewCollections = {
                    navController.navigateToTab(NavRoute.CollectionsScreenRoute.route)
                },
            )
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
