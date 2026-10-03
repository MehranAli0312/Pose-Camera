package com.aipose.camera.posematch.ui.screens.bottomSheet

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.aipose.camera.posematch.R
import com.aipose.camera.posematch.ui.common.bounceClick
import com.aipose.camera.posematch.ui.theme.BrandGradient

@Composable
fun ExitPopUp(
    openFullDialogCustom: MutableState<Boolean>,
    callbackExit: () -> Unit,
) {
    if (!openFullDialogCustom.value) return

    Dialog(onDismissRequest = { openFullDialogCustom.value = false }) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(MaterialTheme.colorScheme.secondaryContainer)
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(25.dp),
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Image(
                    painter = painterResource(R.drawable.ic_exit),
                    contentDescription = null,
                    modifier = Modifier.size(60.dp),
                )

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(3.dp),
                ) {
                    Text(
                        text = stringResource(R.string.exitApp),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 18.sp,
                            color = MaterialTheme.colorScheme.onBackground,
                        ),
                        textAlign = TextAlign.Center,
                    )
                    Text(
                        text = stringResource(R.string.exitAppDescription),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        ),
                        textAlign = TextAlign.Center,
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(20.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                ExitDialogButton(
                    text = stringResource(R.string.no),
                    textColor = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier
                        .weight(1f)
                        .background(
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                            shape = RoundedCornerShape(40.dp),
                        ),
                    onClick = { openFullDialogCustom.value = false },
                )

                ExitDialogButton(
                    text = stringResource(R.string.yes),
                    textColor = Color.White,
                    modifier = Modifier
                        .weight(1f)
                        .background(
                            brush = BrandGradient.brush,
                            shape = RoundedCornerShape(40.dp),
                        ),
                    onClick = callbackExit,
                )
            }
        }
    }
}

@Composable
fun ExitDialogButton(
    text: String,
    textColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .height(36.dp)
            .bounceClick(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.SemiBold,
                fontSize = 12.sp,
                color = textColor,
            ),
            textAlign = TextAlign.Center,
        )
    }
}
