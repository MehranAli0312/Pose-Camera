package com.aipose.camera.posematch.ui.screens.captureDetail

import android.content.Intent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.aipose.camera.posematch.R
import com.aipose.camera.posematch.ui.common.PoseImage
import com.aipose.camera.posematch.ui.common.StudioTopBar
import com.aipose.camera.posematch.ui.screens.captureDetail.components.CaptureDeleteDialog
import com.aipose.camera.posematch.ui.screens.captureDetail.components.CaptureMetadataCard
import com.aipose.camera.posematch.ui.vm.CollectionsViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun CaptureDetailScreen(
    navController: NavHostController,
    captureId: Long,
    viewModel: CollectionsViewModel = koinViewModel(),
) {
    val captureUi by viewModel.captureFor(captureId).collectAsStateWithLifecycle()
    val context = LocalContext.current
    var isDeleteDialogVisible by remember { mutableStateOf(false) }

    val capture = captureUi ?: return
    val shareChooserTitle = stringResource(R.string.share_frame)
    val shareText = stringResource(
        R.string.capture_share_text,
        capture.titleLabel,
        capture.locationLabel,
        capture.capture.matchScore,
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .navigationBarsPadding()
    ) {
        StudioTopBar(
            title = capture.titleLabel,
            onBack = { navController.popBackStack() },
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            contentAlignment = Alignment.Center,
        ) {
            PoseImage(
                imagePath = capture.capture.imagePath,
                contentDescription = capture.titleLabel,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(16.dp)),
                contentScale = ContentScale.Fit,
            )
        }

        CaptureMetadataCard(
            captureUi = capture,
            onShare = {
                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = PLAIN_TEXT_MIME_TYPE
                    putExtra(Intent.EXTRA_TEXT, shareText)
                }
                context.startActivity(Intent.createChooser(shareIntent, shareChooserTitle))
            },
            onDelete = { isDeleteDialogVisible = true },
        )
    }

    if (isDeleteDialogVisible) {
        CaptureDeleteDialog(
            onConfirm = {
                isDeleteDialogVisible = false
                viewModel.delete(capture.capture)
                navController.popBackStack()
            },
            onDismiss = { isDeleteDialogVisible = false },
        )
    }
}

private const val PLAIN_TEXT_MIME_TYPE = "text/plain"
