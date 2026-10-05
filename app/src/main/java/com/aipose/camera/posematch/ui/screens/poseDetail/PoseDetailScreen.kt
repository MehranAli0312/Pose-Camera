package com.aipose.camera.posematch.ui.screens.poseDetail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.aipose.camera.posematch.R
import com.aipose.camera.posematch.ui.common.adaptiveWidth
import com.aipose.camera.posematch.ui.common.shareImageFile
import com.aipose.camera.posematch.ui.graph.NavRoute
import com.aipose.camera.posematch.ui.graph.navigateOnClick
import com.aipose.camera.posematch.ui.graph.popBackStackOnClick
import com.aipose.camera.posematch.ui.screens.poseDetail.components.PoseDetailHero
import com.aipose.camera.posematch.ui.screens.poseDetail.components.PoseDetailPanel
import com.aipose.camera.posematch.ui.screens.poseDetail.models.PoseDetailUiState
import com.aipose.camera.posematch.ui.theme.PoseSheetBottom
import com.aipose.camera.posematch.ui.vm.PoseDetailViewModel
import org.koin.androidx.compose.koinViewModel

private const val PHOTO_VISIBLE_FRACTION = 470f / 844f
private val PhotoVisibleMin = 280.dp
private val PhotoVisibleMax = 560.dp
private val PanelOverlap = 70.dp

@Composable
fun PoseDetailScreen(
    navController: NavHostController,
    poseId: Int,
    viewModel: PoseDetailViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val shareImagePath by viewModel.shareImagePath.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val shareChooserTitle = stringResource(R.string.action_share)

    LaunchedEffect(poseId) { viewModel.onPoseRequested(poseId) }

    LaunchedEffect(shareImagePath) {
        val path = shareImagePath ?: return@LaunchedEffect
        viewModel.consumeShareRequest()
        context.shareImageFile(path, shareChooserTitle)
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(PoseSheetBottom),
    ) {
        val content = uiState as? PoseDetailUiState.Content ?: return@BoxWithConstraints
        val photoVisibleHeight = (maxHeight * PHOTO_VISIBLE_FRACTION).coerceIn(PhotoVisibleMin, PhotoVisibleMax)
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .adaptiveWidth()
                .verticalScroll(rememberScrollState()),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(photoVisibleHeight),
            ) {
                PoseDetailHero(
                    imagePath = content.pose.imagePath,
                    category = content.pose.category,
                    isSaved = content.isSaved,
                    onBack = { navController.popBackStackOnClick() },
                    onToggleSaved = viewModel::toggleSaved,
                    onShare = { viewModel.share(content.pose) },
                    modifier = Modifier.requiredHeight(photoVisibleHeight + PanelOverlap),
                )
            }
            PoseDetailPanel(
                content = content,
                onStartPosing = {
                    navController.navigateOnClick(
                        NavRoute.CameraScreenRoute.routeFor(content.pose.id)
                    )
                },
            )
        }
    }
}
