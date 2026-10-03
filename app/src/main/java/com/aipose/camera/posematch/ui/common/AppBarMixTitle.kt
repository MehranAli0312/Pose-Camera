package com.aipose.camera.posematch.ui.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppBarMixTitle(
    text: AnnotatedString,
    subTitle: String,
    modifier: Modifier = Modifier,
    mContainerColor: Color = MaterialTheme.colorScheme.surface,
    mTitleColor: Color = MaterialTheme.colorScheme.onSurface,
    mSubTitleColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    navItem: (@Composable () -> Unit)? = null,
    menuItems: (@Composable () -> Unit)? = null
) {

    TopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(containerColor = mContainerColor),
        title = {
            Column(
                modifier = Modifier.padding(start = 10.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = text,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontSize = 18.sp,
                        color = mTitleColor
                    ),
                    modifier = modifier

                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = subTitle,
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = mSubTitleColor
                    ),
                    modifier = modifier

                )
            }
        },
        navigationIcon = {
            if (navItem != null) {
                Row {
                    Spacer(modifier = Modifier.width(8.dp))
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
