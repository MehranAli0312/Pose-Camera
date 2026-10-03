package com.aipose.camera.posematch.ui.screens.photoSuccess

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Share
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.aipose.camera.posematch.R
import com.aipose.camera.posematch.ui.common.imageModelOf
import com.aipose.camera.posematch.ui.graph.NavRoute
import com.aipose.camera.posematch.ui.graph.navigateOnClick
import com.aipose.camera.posematch.ui.screens.photoSuccess.components.PhotoSuccessAction
import com.aipose.camera.posematch.ui.screens.photoSuccess.components.PhotoSuccessBanner
import java.io.File

private const val IMAGE_MIME_TYPE = "image/*"
private const val FILE_PROVIDER_SUFFIX = ".fileprovider"

@Composable
fun PhotoSuccessScreen(
    navController: NavHostController,
    photoPath: String,
) {
    val context = LocalContext.current
    val shareChooserTitle = stringResource(R.string.share_frame)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        AsyncImage(
            model = imageModelOf(photoPath),
            contentDescription = stringResource(R.string.saved_photo),
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Fit,
        )

        PhotoSuccessBanner(modifier = Modifier.align(Alignment.TopCenter))

        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 26.dp),
            horizontalArrangement = Arrangement.spacedBy(36.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            PhotoSuccessAction(
                icon = Icons.Default.Home,
                label = stringResource(R.string.nav_home),
                onClick = {
                    navController.navigateOnClick(NavRoute.HomeScreenRoute.route)
                },
            )
            PhotoSuccessAction(
                icon = Icons.Default.Share,
                label = stringResource(R.string.action_share),
                onClick = {
                    runCatching {
                        val uri = FileProvider.getUriForFile(
                            context,
                            context.packageName + FILE_PROVIDER_SUFFIX,
                            File(photoPath),
                        )
                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = IMAGE_MIME_TYPE
                            putExtra(Intent.EXTRA_STREAM, uri)
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }
                        context.startActivity(Intent.createChooser(shareIntent, shareChooserTitle))
                    }
                },
            )
        }
    }
}
