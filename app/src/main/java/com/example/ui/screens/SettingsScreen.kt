package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.DeleteForever
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.School
import androidx.compose.material.icons.rounded.Vibration
import androidx.compose.material.icons.rounded.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.PlayerSettingsState
import com.example.domain.AppLanguage
import com.example.domain.GraphicsQuality
import com.example.ui.theme.CobaltDark
import com.example.ui.theme.CobaltPrimary
import com.example.ui.theme.HeartCrimson
import com.example.ui.theme.HeartCrimsonLight
import com.example.ui.theme.IceSurface
import com.example.ui.theme.PureWhite
import com.example.ui.theme.SoftBorderBlue

@Composable
fun SettingsScreen(
    settings: PlayerSettingsState,
    onMusicToggle: (Boolean) -> Unit,
    onSfxToggle: (Boolean) -> Unit,
    onVibrationToggle: (Boolean) -> Unit,
    onLanguageSelected: (AppLanguage) -> Unit,
    onGraphicsSelected: (GraphicsQuality) -> Unit,
    onReplayTutorial: () -> Unit,
    onResetProgress: () -> Unit,
    onBack: () -> Unit
) {
    var showLanguageMenu by remember { mutableStateOf(false) }
    var showResetConfirm by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(IceSurface)
            .windowInsetsPadding(WindowInsets.safeDrawing),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 600.dp)
                .fillMaxSize()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(PureWhite)
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.testTag("settings_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = "Back",
                        tint = CobaltPrimary
                    )
                }
                Text(
                    text = "Settings",
                    style = MaterialTheme.typography.headlineSmall,
                    color = CobaltDark
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Audio & Haptics
                Card(
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = PureWhite),
                    border = BorderStroke(1.dp, SoftBorderBlue)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Audio & Haptics",
                            style = MaterialTheme.typography.titleMedium,
                            color = CobaltDark
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        SettingsSwitchRow(
                            icon = Icons.Rounded.MusicNote,
                            title = "Music",
                            checked = settings.musicEnabled,
                            testTag = "switch_music",
                            onCheckedChange = onMusicToggle
                        )
                        SettingsSwitchRow(
                            icon = Icons.Rounded.VolumeUp,
                            title = "Sound Effects",
                            checked = settings.soundEffectsEnabled,
                            testTag = "switch_sfx",
                            onCheckedChange = onSfxToggle
                        )
                        SettingsSwitchRow(
                            icon = Icons.Rounded.Vibration,
                            title = "Vibration",
                            checked = settings.vibrationEnabled,
                            testTag = "switch_vibration",
                            onCheckedChange = onVibrationToggle
                        )
                    }
                }

                // Language & Graphics
                Card(
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = PureWhite),
                    border = BorderStroke(1.dp, SoftBorderBlue)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Language & Graphics",
                            style = MaterialTheme.typography.titleMedium,
                            color = CobaltDark
                        )

                        Box {
                            OutlinedButton(
                                onClick = { showLanguageMenu = true },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("language_selector_button"),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Icon(Icons.Rounded.Language, contentDescription = null)
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "Language: ${settings.language.displayName}",
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            DropdownMenu(
                                expanded = showLanguageMenu,
                                onDismissRequest = { showLanguageMenu = false }
                            ) {
                                AppLanguage.entries.forEach { lang ->
                                    DropdownMenuItem(
                                        text = { Text(lang.displayName) },
                                        onClick = {
                                            onLanguageSelected(lang)
                                            showLanguageMenu = false
                                        }
                                    )
                                }
                            }
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Rounded.GraphicEq, contentDescription = null, tint = CobaltPrimary)
                            Text(
                                text = "Graphics Quality",
                                style = MaterialTheme.typography.titleMedium,
                                color = CobaltDark
                            )
                        }

                        GraphicsQuality.entries.forEach { quality ->
                            Surface(
                                onClick = { onGraphicsSelected(quality) },
                                shape = RoundedCornerShape(12.dp),
                                color = IceSurface,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("graphics_${quality.name}")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(
                                        selected = settings.graphicsQuality == quality,
                                        onClick = { onGraphicsSelected(quality) }
                                    )
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(quality.label, style = MaterialTheme.typography.labelLarge)
                                        Text(
                                            quality.description,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Tutorial & Progress
                Card(
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = PureWhite),
                    border = BorderStroke(1.dp, SoftBorderBlue)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = onReplayTutorial,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("replay_tutorial_button"),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = CobaltPrimary)
                        ) {
                            Icon(Icons.Rounded.School, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Replay Tutorial", style = MaterialTheme.typography.titleMedium)
                        }

                        OutlinedButton(
                            onClick = { showResetConfirm = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("reset_progress_button"),
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(1.2.dp, HeartCrimson),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = HeartCrimsonLight,
                                contentColor = HeartCrimson
                            )
                        ) {
                            Icon(Icons.Rounded.DeleteForever, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Reset Progress", style = MaterialTheme.typography.titleMedium)
                        }
                    }
                }
            }
        }
    }

    if (showResetConfirm) {
        AlertDialog(
            onDismissRequest = { showResetConfirm = false },
            title = { Text("Reset All Progress?", fontWeight = FontWeight.Bold) },
            text = {
                Text("This will erase all completed levels, stars, daily challenge history, tournament scores, and achievements. This cannot be undone.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onResetProgress()
                        showResetConfirm = false
                    },
                    modifier = Modifier.testTag("confirm_reset_button")
                ) {
                    Text("Reset", color = HeartCrimson, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetConfirm = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun SettingsSwitchRow(
    icon: ImageVector,
    title: String,
    checked: Boolean,
    testTag: String,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = CobaltPrimary)
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.weight(1f)
        )
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            modifier = Modifier.testTag(testTag)
        )
    }
}
