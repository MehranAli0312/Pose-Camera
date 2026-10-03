package com.aipose.camera.posematch.ui.common

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppBar(
    text: String,
    modifier: Modifier = Modifier,
    mContainerColor: Color = MaterialTheme.colorScheme.surface,
    mTitleColor: Color = MaterialTheme.colorScheme.onSurface,
    navItem: (@Composable () -> Unit)? = null,
    menuItems: (@Composable () -> Unit)? = null
) {

    TopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(containerColor = mContainerColor),
        title = {
            Text(
                text = text,
                style = MaterialTheme.typography.titleSmall.copy(
                    fontSize = 16.sp,
                    color = mTitleColor
                ),
                modifier = modifier

            )
        },
        navigationIcon = {
            if (navItem != null) {
                Row {
                    Spacer(modifier = Modifier.width(4.dp))
                    navItem()
                }
            }
        },
        actions = {
            if (menuItems != null) {
                Row {
                    menuItems()
                    Spacer(modifier = Modifier.width(14.dp))
                }
            }
        })
}
