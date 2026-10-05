package com.aipose.camera.posematch.ui.screens.captureDetail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import com.aipose.camera.posematch.ui.common.safeTopSystemBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.aipose.camera.posematch.R
import com.aipose.camera.posematch.ui.common.PoseDeleteDialog
import com.aipose.camera.posematch.ui.common.PoseImage
import com.aipose.camera.posematch.ui.common.adaptiveWidth
import com.aipose.camera.posematch.ui.common.shareImageFile
import com.aipose.camera.posematch.ui.graph.NavRoute
import com.aipose.camera.posematch.ui.graph.navigateOnClick
import com.aipose.camera.posematch.ui.graph.popBackStackOnClick
import com.aipose.camera.posematch.ui.screens.captureDetail.components.PhotoDetailActions
import com.aipose.camera.posematch.ui.screens.captureDetail.components.PhotoDetailMatchBadge
import com.aipose.camera.posematch.ui.screens.captureDetail.components.PhotoDetailSheet
import com.aipose.camera.posematch.ui.screens.captureDetail.components.PhotoDetailTopBar
import com.aipose.camera.posematch.ui.screens.captureDetail.models.PhotoDetailInfo
import com.aipose.camera.posematch.ui.screens.collections.models.CaptureUi
import com.aipose.camera.posematch.ui.models.GlossyBadgePalette
import com.aipose.camera.posematch.ui.models.badgeForCategory
import com.aipose.camera.posematch.ui.theme.PoseNightBottom
import com.aipose.camera.posematch.ui.theme.PosePhotoScrim
import com.aipose.camera.posematch.ui.vm.CollectionsViewModel
import com.aipose.camera.posematch.util.bidiIsolate
import org.koin.androidx.compose.koinViewModel

private const val PHOTO_HEIGHT_FRACTION = 560f / 844f
private val TopScrimHeight = 170.dp
private val BottomScrimHeight = 200.dp

@Composable
fun CaptureDetailScreen(
    navController: NavHostController,
    captureId: Long,
    viewModel: CollectionsViewModel = koinViewModel(),
) {
    val captureUi by viewModel.captureFor(captureId).collectAsStateWithLifecycle()
    val context = LocalContext.current
    var isDeleteDialogVisible by remember { mutableStateOf(false) }
    val shareChooserTitle = stringResource(R.string.share_frame)

    val capture = captureUi ?: return

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(PoseNightBottom)
    ) {
        PhotoBackdrop(captureUi = capture)

        PhotoDetailTopBar(
            isFavorite = capture.capture.isFavorite,
            onBack = navController::popBackStackOnClick,
            onToggleFavorite = { viewModel.toggleFavorite(capture.capture) },
            modifier = Modifier
                .align(Alignment.TopCenter)
                .safeTopSystemBarsPadding(),
        )

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .adaptiveWidth(),
        ) {
            if (capture.capture.poseId != null && capture.capture.matchScore > 0) {
                PhotoDetailMatchBadge(
                    score = capture.capture.matchScore,
                    modifier = Modifier.padding(start = 24.dp, bottom = 38.dp),
                )
            }
            PhotoDetailSheet(
                title = capture.titleLabel,
                subtitle = stringResource(R.string.detail_saved_in, capture.locationLabel),
                infos = photoDetailInfos(capture),
            ) {
                PhotoDetailActions(
                    onShare = { context.shareImageFile(capture.capture.imagePath, shareChooserTitle) },
                    onReshoot = {
                        val route = capture.capture.poseId?.let(NavRoute.CameraScreenRoute::routeFor)
                            ?: NavRoute.CameraScreenRoute.routeWithoutPose()
                        navController.navigateOnClick(route)
                    },
                    onDelete = { isDeleteDialogVisible = true },
                )
            }
        }
    }

    if (isDeleteDialogVisible) {
        PoseDeleteDialog(
            title = stringResource(R.string.delete_title),
            message = stringResource(R.string.delete_message),
            onConfirm = {
                isDeleteDialogVisible = false
                viewModel.delete(capture.capture)
                navController.popBackStack()
            },
            onDismiss = { isDeleteDialogVisible = false },
        )
    }
}

@Composable
private fun PhotoBackdrop(captureUi: CaptureUi) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(PHOTO_HEIGHT_FRACTION)
    ) {
        PoseImage(
            imagePath = captureUi.capture.imagePath,
            contentDescription = captureUi.titleLabel,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
        )
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .height(TopScrimHeight)
                .background(Brush.verticalGradient(listOf(Color.Black.copy(alpha = 0.6f), Color.Transparent)))
        )
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(BottomScrimHeight)
                .background(Brush.verticalGradient(listOf(Color.Transparent, PosePhotoScrim.copy(alpha = 0.92f))))
        )
    }
}

@Composable
private fun photoDetailInfos(captureUi: CaptureUi): List<PhotoDetailInfo> {
    val capture = captureUi.capture
    val categoryBadge = badgeForCategory(capture.category)
    return listOf(
        PhotoDetailInfo(
            iconRes = R.drawable.ic_pose_target,
            palette = GlossyBadgePalette.Emerald,
            glyphSize = 28.dp,
            label = stringResource(R.string.detail_match_score),
            value = stringResource(R.string.score_percent, capture.matchScore).bidiIsolate(),
        ),
        PhotoDetailInfo(
            iconRes = categoryBadge.iconRes,
            palette = categoryBadge.palette,
            glyphSize = 28.dp,
            label = stringResource(R.string.detail_category),
            value = capture.category.ifBlank { stringResource(R.string.camera_no_pose) },
        ),
        PhotoDetailInfo(
            iconRes = R.drawable.ic_saved_pin,
            palette = GlossyBadgePalette.Cyan,
            glyphSize = 14.dp,
            label = stringResource(R.string.detail_location),
            value = captureUi.locationLabel,
        ),
        PhotoDetailInfo(
            iconRes = R.drawable.ic_camera_timer,
            palette = GlossyBadgePalette.Amber,
            glyphSize = 13.dp,
            label = stringResource(R.string.detail_captured),
            value = captureUi.capturedLabel,
        ),
    )
}
