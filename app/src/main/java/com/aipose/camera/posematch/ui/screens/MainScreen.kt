package com.aipose.camera.posematch.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.aipose.camera.posematch.ui.common.AppSystemBars
import com.aipose.camera.posematch.ui.graph.AppNavGraph
import com.aipose.camera.posematch.ui.theme.MyAppTheme
import com.aipose.camera.posematch.ui.vm.LanguageViewModel
import org.koin.compose.koinInject

@Composable
fun MainScreen(
    languageChangeViewModel: LanguageViewModel = koinInject(),
) {
    AppSystemBars(isDarkTheme = true)

    MyAppTheme {

        val currentLanguageCode = languageChangeViewModel.currentLanguageCode
        val isLanguageLoaded = languageChangeViewModel.isLanguageLoaded

        if (isLanguageLoaded) {
            remember(currentLanguageCode) {
                if (currentLanguageCode.isNotBlank()) {
                    languageChangeViewModel.applyLanguage(currentLanguageCode)
                }
                true
            }

            val navParentController = rememberNavController()
            Box(modifier = Modifier.background(MaterialTheme.colorScheme.background)) {
                AppNavGraph(
                    navParentController = navParentController
                )
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        MaterialTheme.colorScheme.background
                    )
            )
        }
    }
}
