package com.aipose.camera.posematch.ui.screens.home.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.aipose.camera.posematch.R
import com.aipose.camera.posematch.ui.common.ProAppBarAction

@Composable
internal fun HomeTopBar(
    onImportPose: () -> Unit,
    onOpenPro: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(R.string.home_title),
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            letterSpacing = (-0.5).sp,
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            ProAppBarAction(onClick = onOpenPro)
            HomeActionIcon(
                icon = Icons.Default.AddPhotoAlternate,
                contentDescription = stringResource(R.string.action_import),
                onClick = onImportPose,
            )
        }
    }
}
