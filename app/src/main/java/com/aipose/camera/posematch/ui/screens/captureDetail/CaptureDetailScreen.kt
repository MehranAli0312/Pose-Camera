package com.aipose.camera.posematch.ui.screens.captureDetail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.aipose.camera.posematch.domain.models.AnalyticsEvent
import com.aipose.camera.posematch.ui.common.PoseDeleteDialog
import com.aipose.camera.posematch.ui.common.PoseImage
import com.aipose.camera.posematch.ui.common.adaptiveWidth
import com.aipose.camera.posematch.ui.common.safeTopSystemBarsPadding
import com.aipose.camera.posematch.ui.common.shareImageFile
import com.aipose.camera.posematch.ui.graph.popBackStackOnClick
import com.aipose.camera.posematch.ui.screens.captureDetail.components.PhotoDetailActions
import com.aipose.camera.posematch.ui.screens.captureDetail.components.PhotoDetailTopBar
import com.aipose.camera.posematch.ui.screens.collections.models.CaptureUi
import com.aipose.camera.posematch.ui.theme.PoseNightBottom
import com.aipose.camera.posematch.ui.vm.CollectionsViewModel
import org.koin.androidx.compose.koinViewModel

private val TopScrimHeight = 170.dp

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

    LaunchedEffect(captureId) { viewModel.trackEvent(AnalyticsEvent.CaptureDetail) }

    val capture = captureUi ?: return

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(PoseNightBottom)
    ) {
        PhotoBackdrop(captureUi = capture)

        PhotoDetailTopBar(
            onBack = navController::popBackStackOnClick,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .safeTopSystemBarsPadding(),
        )

        PhotoDetailActions(
            onSend = {
                viewModel.trackEvent(AnalyticsEvent.CaptureShared)
                context.shareImageFile(capture.capture.imagePath, shareChooserTitle)
            },
            onDelete = { isDeleteDialogVisible = true },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .adaptiveWidth(),
        )
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
        modifier = Modifier.fillMaxSize()
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
    }
}
