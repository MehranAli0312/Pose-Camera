package com.aipose.camera.posematch.ui.screens

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.*
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aipose.camera.posematch.ui.viewmodel.MainViewModel
import java.util.*
import com.aipose.camera.posematch.R
import androidx.activity.compose.BackHandler
import androidx.compose.ui.res.stringResource

@Composable
fun SettingsScreen(viewModel: MainViewModel) {
    val context = LocalContext.current
    val language by viewModel.selectedLanguage.collectAsState()
    val theme by viewModel.appTheme.collectAsState()

    var showLanguageScreen by remember { mutableStateOf(false) }
    var showThemeDialog by remember { mutableStateOf(false) }
    var pendingTheme by remember { mutableStateOf(theme) }

    val themes = listOf("Dark", "Light", "Sleek Charcoal", "Cyberpunk Violet")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(16.dp)
    ) {
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = stringResource(R.string.nav_settings),
            style = MaterialTheme.typography.headlineSmall.copy(
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        )

        Spacer(modifier = Modifier.height(18.dp))

        // All settings as a single uniform card stack (no section sub-headings).
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            SettingCard(
                icon = Icons.Default.Language,
                title = stringResource(R.string.settings_language),
                onClick = { showLanguageScreen = true }) {
                Text(language, color = AccentCopper, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
            SettingCard(
                icon = Icons.Default.Palette,
                title = stringResource(R.string.settings_theme),
                onClick = { pendingTheme = theme; showThemeDialog = true }) {
                Text(theme, color = AccentCopper, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
            SettingCard(
                icon = Icons.Default.Share,
                title = stringResource(R.string.settings_share),
                onClick = {
                    val shareTextIntent = Intent().apply {
                        action = Intent.ACTION_SEND
                        type = "text/plain"
                        putExtra(
                            Intent.EXTRA_TEXT,
                            "Download Pose Match Camera: achieve perfect posture alignment live on-device!"
                        )
                    }
                    context.startActivity(Intent.createChooser(shareTextIntent, "Share App"))
                }
            ) {
                Icon(
                    Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = Color.Gray,
                    modifier = Modifier.size(20.dp)
                )
            }
            SettingCard(
                icon = Icons.Default.PrivacyTip,
                title = stringResource(R.string.settings_privacy),
                onClick = {
                    runCatching {
                        context.startActivity(
                            Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse("https://sites.google.com/view/posematchcamera/home")
                            )
                        )
                    }
                }
            ) {
                Icon(
                    Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = Color.Gray,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }

    // Language: reuse the first-run splash language screen. Shown in a full-screen Dialog so
    // it covers the bottom navigation bar (identical to the onboarding presentation).
    if (showLanguageScreen) {
        Dialog(
            onDismissRequest = { showLanguageScreen = false },
            properties = DialogProperties(
                usePlatformDefaultWidth = false,
                dismissOnClickOutside = false
            )
        ) {
            LanguageSelectionScreen(
                viewModel = viewModel,
                onNavigateNext = {
                    showLanguageScreen = false
                    // Re-apply locale so the change (incl. RTL direction) takes effect immediately.
                    (context as? Activity)?.recreate()
                }
            )
        }
    }

    // Theme picker — choose a style, then Apply or Cancel (no instant commit).
    if (showThemeDialog) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.75f)),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .width(300.dp)
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SoftCardGray)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        stringResource(R.string.settings_select_theme),
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        themes.forEach { themeOption ->
                            val isSelected = themeOption == pendingTheme
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) AccentCopper.copy(alpha = 0.2f) else Color.Transparent)
                                    .clickable { pendingTheme = themeOption }
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(themeOption, color = Color.White, fontSize = 14.sp)
                                if (isSelected) {
                                    Icon(
                                        Icons.Default.Check,
                                        contentDescription = "Selected",
                                        tint = AccentCopper,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = { showThemeDialog = false },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, GlassWhite)
                        ) {
                            Text(stringResource(R.string.action_cancel), color = Color.White)
                        }
                        Button(
                            onClick = {
                                viewModel.setTheme(pendingTheme)
                                showThemeDialog = false
                                Toast.makeText(
                                    context,
                                    context.getString(R.string.toast_theme_changed, pendingTheme),
                                    Toast.LENGTH_SHORT
                                ).show()
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = AccentCopper),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(stringResource(R.string.action_apply), color = Color.White)
                        }
                    }
                }
            }
        }
        BackHandler { showThemeDialog = false }
    }
}
// Uniform compact settings row used for every entry (config + help).
@Composable
internal fun SettingCard(
    icon: ImageVector,
    title: String,
    onClick: () -> Unit,
    trailing: @Composable () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(54.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(SoftCardGray)
            .clickable { onClick() }
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    icon,
                    contentDescription = title,
                    tint = Color.LightGray,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(title, color = Color.White, fontSize = 14.sp)
            }
            trailing()
        }
    }
}
