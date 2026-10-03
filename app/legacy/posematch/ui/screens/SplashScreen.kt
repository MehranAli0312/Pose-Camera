package com.aipose.camera.posematch.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.aipose.camera.posematch.ui.viewmodel.MainViewModel
import kotlinx.coroutines.delay
import java.util.*
import com.aipose.camera.posematch.R
import androidx.compose.ui.res.stringResource
import androidx.core.graphics.drawable.toBitmap

@Composable
fun SplashScreen(
    viewModel: MainViewModel,
    onNavigateNext: (String) -> Unit
) {
    val onboardingCompleted by viewModel.onboardingCompleted.collectAsState()
    var appLogoVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(300)
        appLogoVisible = true
        delay(2200)
        if (onboardingCompleted) {
            onNavigateNext(Routes.MAIN_CONTAINER)
        } else {
            onNavigateNext(Routes.ONBOARDING)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(StudioBackgroundGradient),
        contentAlignment = Alignment.Center
    ) {
        AnimatedVisibility(
            visible = appLogoVisible,
            enter = fadeIn(animationSpec = tween(1500, easing = LinearOutSlowInEasing)) +
                    scaleIn(
                        initialScale = 0.85f,
                        animationSpec = tween(1500, easing = LinearOutSlowInEasing)
                    ),
            exit = fadeOut(animationSpec = tween(500))
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // App launcher icon (mipmap) as the splash logo. Loaded as a bitmap because
                // on API 26+ the launcher icon is an adaptive-icon XML, which painterResource
                // cannot render (it supports only vectors/raster).
                val context = LocalContext.current
                val logoBitmap = remember {
                    ContextCompat.getDrawable(context, R.mipmap.ic_launcher)
                        ?.toBitmap(216, 216)
                        ?.asImageBitmap()
                }
                if (logoBitmap != null) {
                    Image(
                        bitmap = logoBitmap,
                        contentDescription = "App Logo",
                        modifier = Modifier
                            .size(110.dp)
                            .clip(RoundedCornerShape(28.dp))
                            .border(1.5.dp, AccentCopper, RoundedCornerShape(28.dp)),
                        contentScale = ContentScale.Crop
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "POSE MATCH CAMERA",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 3.sp,
                        fontFamily = FontFamily.SansSerif
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = stringResource(R.string.splash_tagline),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = Color.Gray,
                        letterSpacing = 1.sp
                    )
                )
            }
        }
    }
}
