package com.aipose.camera.posematch.ui.screens.poseAlbum

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.aipose.camera.posematch.ui.common.POSE_ROW_SIZE
import com.aipose.camera.posematch.ui.common.PoseThumbRow
import com.aipose.camera.posematch.ui.common.StudioTopBar
import com.aipose.camera.posematch.ui.graph.NavRoute
import com.aipose.camera.posematch.ui.graph.navigateOnClick
import com.aipose.camera.posematch.ui.vm.PoseAlbumViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun PoseAlbumScreen(
    navController: NavHostController,
    category: String,
    viewModel: PoseAlbumViewModel = koinViewModel(),
) {
    val poses by viewModel.posesFor(category).collectAsStateWithLifecycle()

    Column(modifier = Modifier.fillMaxSize()) {
        StudioTopBar(title = category, onBack = { navController.popBackStack() })
        Spacer(modifier = Modifier.height(6.dp))
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(bottom = 24.dp),
        ) {
            poses.chunked(POSE_ROW_SIZE).forEachIndexed { index, rowPoses ->
                item(key = ALBUM_ROW_KEY + index) {
                    PoseThumbRow(
                        poses = rowPoses,
                        onPoseClick = { pose ->
                            navController.navigateOnClick(
                                NavRoute.CameraScreenRoute.routeFor(pose.id)
                            )
                        },
                    )
                }
            }
        }
    }
}

private const val ALBUM_ROW_KEY = "pose_album_row_"
