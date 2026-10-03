package com.aipose.camera.posematch.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.aipose.camera.posematch.ui.viewmodel.MainViewModel
import java.util.*
import com.aipose.camera.posematch.data.LocaleHelper
import com.aipose.camera.posematch.R
import androidx.compose.ui.res.stringResource

@Composable
fun LanguageSelectionScreen(
    viewModel: MainViewModel,
    onNavigateNext: (String) -> Unit
) {
    val context = LocalContext.current
    val selectedLanguage by viewModel.selectedLanguage.collectAsState()
    val languages = listOf(
        "English", "Hindi", "Urdu", "Arabic", "Spanish",
        "Turkish", "Bangla", "French", "Portuguese", "Russian", "Filipino", "German"
    )

    fun confirm() {
        LocaleHelper.persistLanguage(context, selectedLanguage)
        onNavigateNext(Routes.MAIN_CONTAINER)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(StudioBackgroundGradient)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp)
        ) {
            // Top bar — small, compact circular Done button on the right (no big icon, no Continue).
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(AccentCopper)
                        .clickable { confirm() }
                        .testTag("language_confirm_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = "Done",
                        tint = Color.White,
                        modifier = Modifier.size(17.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(15.dp))

            Text(
                text = stringResource(R.string.lang_choose_title),
                style = MaterialTheme.typography.headlineSmall.copy(
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = stringResource(R.string.lang_choose_subtitle),
                style = MaterialTheme.typography.bodyMedium.copy(color = Color.Gray)
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Scrollable compact language list.
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                languages.forEach { lang ->
                    val isSelected = selectedLanguage == lang
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(if (isSelected) AccentCopper.copy(alpha = 0.15f) else SoftCardGray)
                            .border(
                                1.5.dp,
                                if (isSelected) AccentCopper else Color.Transparent,
                                RoundedCornerShape(14.dp)
                            )
                            .clickable { viewModel.setLanguage(lang) }
                            .testTag("lang_${lang.lowercase()}"),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = lang,
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    color = Color.White,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            )
                            RadioButton(
                                selected = isSelected,
                                onClick = { viewModel.setLanguage(lang) },
                                colors = RadioButtonDefaults.colors(
                                    selectedColor = AccentCopper,
                                    unselectedColor = Color.Gray
                                )
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}
