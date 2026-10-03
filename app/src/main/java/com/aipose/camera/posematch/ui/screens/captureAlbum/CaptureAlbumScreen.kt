package com.aipose.camera.posematch.ui.screens.captureAlbum

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.aipose.camera.posematch.R
import com.aipose.camera.posematch.ui.common.StudioTopBar
import com.aipose.camera.posematch.ui.graph.NavRoute
import com.aipose.camera.posematch.ui.graph.navigateOnClick
import com.aipose.camera.posematch.ui.screens.collections.components.CAPTURE_ROW_SIZE
import com.aipose.camera.posematch.ui.screens.collections.components.CaptureRow
import com.aipose.camera.posematch.ui.theme.LocalAppPalette
import com.aipose.camera.posematch.ui.vm.CollectionsViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun CaptureAlbumScreen(
    navController: NavHostController,
    locationLabel: String,
    viewModel: CollectionsViewModel = koinViewModel(),
) {
    val album by viewModel.albumFor(locationLabel).collectAsStateWithLifecycle()

    Column(modifier = Modifier.fillMaxSize()) {
        StudioTopBar(
            title = locationLabel,
            onBack = { navController.popBackStack() },
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(start = 20.dp, bottom = 4.dp),
        ) {
            Icon(
                imageVector = Icons.Default.LocationOn,
                contentDescription = null,
                tint = LocalAppPalette.current.accent,
                modifier = Modifier.size(16.dp),
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = stringResource(R.string.frames_count, album.count),
                color = Color.Gray,
                fontSize = 11.sp,
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(bottom = 24.dp),
        ) {
            album.captures.chunked(CAPTURE_ROW_SIZE).forEachIndexed { index, rowCaptures ->
                item(key = ALBUM_ROW_KEY + index) {
                    CaptureRow(
                        captures = rowCaptures,
                        onCaptureClick = { captureUi ->
                            navController.navigateOnClick(
                                NavRoute.CaptureDetailScreenRoute.routeFor(captureUi.id)
                            )
                        },
                    )
                }
            }
        }
    }
}

private const val ALBUM_ROW_KEY = "capture_album_row_"
