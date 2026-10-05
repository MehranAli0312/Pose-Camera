package com.aipose.camera.posematch.ui.screens.language

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.aipose.camera.posematch.R
import com.aipose.camera.posematch.ads.LanguageScreenBottom
import com.aipose.camera.posematch.ads.rememberMissedSplashAd
import com.aipose.camera.posematch.ui.common.safeBottomSystemBarsPadding
import com.example.ads.AdPlacement
import com.example.ads.compose.AdsSlot
import com.aipose.camera.posematch.ui.common.PoseScreenGutter
import com.aipose.camera.posematch.ui.common.AppBar
import com.aipose.camera.posematch.ui.common.PoseBackButton
import com.aipose.camera.posematch.ui.common.bounceClick
import com.aipose.camera.posematch.ui.graph.acceptNavigationClick
import com.aipose.camera.posematch.ui.graph.popBackStackOnClick
import com.aipose.camera.posematch.ui.models.allLanguageItems
import com.aipose.camera.posematch.ui.models.resolveDefaultLanguageCode
import com.aipose.camera.posematch.ui.screens.language.components.LanguageItemCard
import com.aipose.camera.posematch.ui.firebaseRemote.AdsRemoteConfigStore
import com.aipose.camera.posematch.ui.screens.splash.goToOnboardOrNext
import com.aipose.camera.posematch.ui.theme.BrandGradient
import com.aipose.camera.posematch.ui.vm.LanguageViewModel
import com.aipose.camera.posematch.ui.vm.SplashViewModel
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject

@Composable
fun LocalizeScreen(
    navController: NavHostController,
    isFirstSession: Boolean = false,
    languageViewModel: LanguageViewModel = koinInject(),
    splashViewModel: SplashViewModel = koinViewModel(),
    remoteConfigStore: AdsRemoteConfigStore = koinInject(),
) {
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val missedSplashAd = rememberMissedSplashAd()

    var selectedLanguage by rememberSaveable {
        mutableStateOf(
            if (isFirstSession) {
                resolveDefaultLanguageCode()
            } else {
                languageViewModel.currentLanguageCode
            }
        )
    }

    val infinite = rememberInfiniteTransition(label = "CleanerCardLux")

    val arrowNudge by infinite.animateFloat(
        initialValue = 0f,
        targetValue = 4f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "arrow",
    )

    LaunchedEffect(isFirstSession) {
        val resolvedCode = if (isFirstSession) {
            val systemLanguageCode = resolveDefaultLanguageCode()
            selectedLanguage = systemLanguageCode
            languageViewModel.changeLanguage(systemLanguageCode)
            languageViewModel.applyLanguage(systemLanguageCode)
            systemLanguageCode
        } else if (languageViewModel.currentLanguageCode.isNotBlank()) {
            selectedLanguage = languageViewModel.currentLanguageCode
            languageViewModel.currentLanguageCode
        } else {
            selectedLanguage
        }

        val index =
            allLanguageItems.indexOfFirst { it.code.equals(resolvedCode, ignoreCase = true) }
        if (index >= 0) {
            listState.scrollToItem(index)
        }
    }

    val applyAndNavigate: () -> Unit = {
        if (isFirstSession) {
            missedSplashAd.showThen(continueWhenShown = false) {
                coroutineScope.launch {
                    val showOnboarding = remoteConfigStore.current.showOnboardingScreen
                    if (!showOnboarding) missedSplashAd.clear()
                    splashViewModel.writeSplashStatus()
                    languageViewModel.changeLanguage(selectedLanguage)
                    languageViewModel.applyLanguage(selectedLanguage)
                    goToOnboardOrNext(
                        navController = navController,
                        showOnboarding = showOnboarding,
                    )
                }
            }
        } else {
            languageViewModel.changeLanguage(selectedLanguage)
            languageViewModel.applyLanguage(selectedLanguage)
            navController.popBackStack()
        }
    }

    BackHandler(enabled = true) {
        if (navController.acceptNavigationClick()) {
            applyAndNavigate()
        }
    }

    Scaffold(
        topBar = {
            AppBar(
                text = stringResource(R.string.settings_language),
                navItem = if (isFirstSession) {
                    null
                } else {
                    {
                        PoseBackButton(onClick = navController::popBackStackOnClick)
                    }
                },
                menuItems = {
                    Row(
                        modifier = Modifier
                            .bounceClick {
                                if (navController.acceptNavigationClick()) {
                                    applyAndNavigate()
                                }
                            }
                            .clip(RoundedCornerShape(50))
                            .background(brush = BrandGradient.brush, shape = RoundedCornerShape(50))
                            .padding(horizontal = 12.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                    ) {
                        Text(
                            text = stringResource(R.string.done),
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White,
                                fontSize = 12.sp,
                            ),
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Icon(
                            painter = painterResource(R.drawable.ic_arrow_forward),
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier
                                .size(20.dp)
                                .offset(x = arrowNudge.dp),
                        )
                    }
                },
            )
        },
        bottomBar = {
            AdsSlot(
                placement = AdPlacement.LanguageScreenBottom,
                modifier = Modifier.safeBottomSystemBarsPadding(),
            )
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
        ) {
            LazyColumn(
                modifier = Modifier.weight(1f),
                state = listState,
                contentPadding = PaddingValues(horizontal = PoseScreenGutter, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                items(
                    items = allLanguageItems,
                    key = { language -> language.code },
                ) { language ->
                    val isSelected = language.code.equals(selectedLanguage, ignoreCase = true)

                    LanguageItemCard(
                        flagRes = language.flag,
                        name = language.name,
                        isSelected = isSelected,
                        onClick = { selectedLanguage = language.code },
                    )
                }
            }
        }
    }
}
