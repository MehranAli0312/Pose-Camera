package com.aipose.camera.posematch.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aipose.camera.posematch.ui.viewmodel.MainViewModel
import java.util.*
import com.aipose.camera.posematch.R
import androidx.compose.ui.res.stringResource

@Composable
fun OnboardingScreen(
    viewModel: MainViewModel,
    onNavigateNext: (String) -> Unit
) {
    var currentPage by remember { mutableStateOf(0) }
    val context = LocalContext.current

    val pageTitles = listOf(
        stringResource(R.string.onboard_title_1),
        stringResource(R.string.onboard_title_2),
        stringResource(R.string.onboard_title_3)
    )

    val pageDescriptions = listOf(
        stringResource(R.string.onboard_desc_1),
        stringResource(R.string.onboard_desc_2),
        stringResource(R.string.onboard_desc_3)
    )

    // Resource mapping for generated illustration jpeg draws
    val drawingResName = when (currentPage) {
        0 -> "pose_guide_onboard1_1781797560457"
        1 -> "pose_guide_onboard2_1781797579198"
        else -> "pose_guide_onboard3_1781797601454"
    }
    val drawingResId =
        context.resources.getIdentifier(drawingResName, "drawable", context.packageName)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(StudioBackgroundGradient)
    ) {
        // Content Area with illustration
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header Row (Skip button)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding(),
                horizontalArrangement = Arrangement.End
            ) {
                if (currentPage < 2) {
                    TextButton(
                        onClick = {
                            viewModel.setOnboardingCompleted()
                            onNavigateNext(Routes.LANGUAGE)
                        },
                        modifier = Modifier.testTag("onboarding_skip_button")
                    ) {
                        Text(
                            stringResource(R.string.action_skip),
                            color = Color.Gray,
                            fontSize = 16.sp
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.height(36.dp))
                }
            }

            // Central image illustration block
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                if (drawingResId != 0) {
                    Image(
                        painter = painterResource(id = drawingResId),
                        contentDescription = "Onboarding Visual",
                        modifier = Modifier
                            .fillMaxHeight(0.85f)
                            .aspectRatio(2f / 3f)
                            .clip(RoundedCornerShape(24.dp))
                            .border(1.dp, GlassWhite, RoundedCornerShape(24.dp)),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    // Modern styled vector fallback
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .fillMaxSize(0.7f)
                            .background(Color(0xFF1F1F26), RoundedCornerShape(24.dp))
                    ) {
                        Icon(
                            imageVector = when (currentPage) {
                                0 -> Icons.Default.Collections
                                1 -> Icons.Default.AccessibilityNew
                                else -> Icons.Default.PhotoCamera
                            },
                            contentDescription = "Illustration Fallback",
                            tint = AccentCopper,
                            modifier = Modifier.size(80.dp)
                        )
                    }
                }
            }

            // Description and navigation control panel
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
            ) {
                Text(
                    text = pageTitles[currentPage],
                    style = MaterialTheme.typography.headlineLarge.copy(
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = pageDescriptions[currentPage],
                    style = MaterialTheme.typography.bodyLarge.copy(
                        color = Color.Gray,
                        textAlign = TextAlign.Center,
                        lineHeight = 22.sp
                    ),
                    modifier = Modifier.padding(horizontal = 12.dp)
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Indicators and Next Action control Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Bullet dots indicators
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        for (i in 0..2) {
                            Box(
                                modifier = Modifier
                                    .size(if (currentPage == i) 18.dp else 8.dp, 8.dp)
                                    .clip(CircleShape)
                                    .background(if (currentPage == i) AccentCopper else Color.DarkGray)
                            )
                        }
                    }

                    // Next / Let's Go Button
                    Button(
                        onClick = {
                            if (currentPage < 2) {
                                currentPage++
                            } else {
                                viewModel.setOnboardingCompleted()
                                onNavigateNext(Routes.LANGUAGE)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AccentCopper),
                        shape = RoundedCornerShape(24.dp),
                        modifier = Modifier
                            .widthIn(min = 120.dp)
                            .testTag("onboarding_next_button")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = if (currentPage == 2) stringResource(R.string.action_lets_go) else stringResource(
                                    R.string.action_next
                                ),
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = "Arrow Next",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
