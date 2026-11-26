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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.HelpOutline
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Lightbulb
import androidx.compose.material.icons.rounded.MilitaryTech
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.PrivacyTip
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.PlayerStatsSummary
import com.example.ui.components.AboutGameDialog
import com.example.ui.components.EditPlayerNameDialog
import com.example.ui.components.HelpDialog
import com.example.ui.components.PrivacyPreferencesDialog
import com.example.ui.components.PrivacyRightsDialog
import com.example.ui.theme.AmberHint
import com.example.ui.theme.CobaltDark
import com.example.ui.theme.CobaltPrimary
import com.example.ui.theme.EmeraldContainer
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.GoldContainer
import com.example.ui.theme.IceBlueContainer
import com.example.ui.theme.IceSurface
import com.example.ui.theme.PureWhite
import com.example.ui.theme.SoftBorderBlue

@Composable
fun ProfileScreen(
    summary: PlayerStatsSummary,
    onOpenSettings: () -> Unit,
    onUpdatePlayerName: (String) -> Unit,
    onUpdatePrivacyPrefs: (Boolean, Boolean) -> Unit,
    onBack: () -> Unit
) {
    var showHelp by remember { mutableStateOf(false) }
    var showAbout by remember { mutableStateOf(false) }
    var showPrivacyRights by remember { mutableStateOf(false) }
    var showPrivacyPrefs by remember { mutableStateOf(false) }
    var showEditName by remember { mutableStateOf(false) }

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
            // Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(PureWhite)
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.testTag("profile_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = "Back",
                        tint = CobaltPrimary
                    )
                }
                Text(
                    text = "Player Profile",
                    style = MaterialTheme.typography.headlineSmall,
                    color = CobaltDark,
                    modifier = Modifier.weight(1f)
                )
                IconButton(
                    onClick = onOpenSettings,
                    modifier = Modifier.testTag("profile_settings_icon")
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Settings,
                        contentDescription = "Settings",
                        tint = CobaltPrimary
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Player Identity Card
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = PureWhite),
                    border = BorderStroke(1.dp, SoftBorderBlue)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = IceBlueContainer,
                            modifier = Modifier.size(60.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Rounded.Person,
                                    contentDescription = null,
                                    tint = CobaltPrimary,
                                    modifier = Modifier.size(34.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = summary.settings.playerName,
                                style = MaterialTheme.typography.titleLarge,
                                color = CobaltDark
                            )
                            Text(
                                text = "Rank: ${summary.rankTitle}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        IconButton(onClick = { showEditName = true }) {
                            Icon(
                                imageVector = Icons.Rounded.Edit,
                                contentDescription = "Edit Name",
                                tint = CobaltPrimary
                            )
                        }
                    }
                }

                // Stats 2x2 Grid
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ProfileStatBox(
                        label = "Puzzles Solved",
                        value = "${summary.completedLevelsCount}",
                        icon = Icons.Rounded.CheckCircle,
                        tint = EmeraldSuccess,
                        modifier = Modifier.weight(1f)
                    )
                    ProfileStatBox(
                        label = "Total Stars",
                        value = "${summary.totalStars}",
                        icon = Icons.Rounded.Star,
                        tint = GoldAccent,
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ProfileStatBox(
                        label = "Total Trophies",
                        value = "${summary.totalTrophies}",
                        icon = Icons.Rounded.EmojiEvents,
                        tint = CobaltPrimary,
                        modifier = Modifier.weight(1f)
                    )
                    ProfileStatBox(
                        label = "Hint Balance",
                        value = "${summary.settings.hintBalance}",
                        icon = Icons.Rounded.Lightbulb,
                        tint = AmberHint,
                        modifier = Modifier.weight(1f)
                    )
                }

                // Awards & Achievements
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = PureWhite),
                    border = BorderStroke(1.dp, SoftBorderBlue)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Rounded.MilitaryTech, contentDescription = null, tint = GoldAccent)
                            Text(
                                text = "Awards & Achievements",
                                style = MaterialTheme.typography.titleMedium,
                                color = CobaltDark
                            )
                        }

                        summary.achievements.forEach { status ->
                            val frac = (status.currentCount / status.def.targetCount.toFloat()).coerceIn(0f, 1f)
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = if (status.unlocked) GoldContainer else IceSurface
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = status.def.title,
                                            style = MaterialTheme.typography.titleMedium,
                                            color = CobaltDark
                                        )
                                        Text(
                                            text = if (status.unlocked) "Unlocked!" else "${status.currentCount}/${status.def.targetCount}",
                                            style = MaterialTheme.typography.labelLarge,
                                            color = if (status.unlocked) EmeraldSuccess else CobaltPrimary
                                        )
                                    }
                                    Text(
                                        text = status.def.description,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    LinearProgressIndicator(
                                        progress = { frac },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(7.dp)
                                            .clip(RoundedCornerShape(50)),
                                        color = if (status.unlocked) EmeraldSuccess else CobaltPrimary,
                                        trackColor = PureWhite
                                    )
                                }
                            }
                        }
                    }
                }

                // Links Section: Settings, Help, About Game, Privacy Rights, Privacy Preferences
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = PureWhite),
                    border = BorderStroke(1.dp, SoftBorderBlue)
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        ProfileLinkRow(
                            icon = Icons.Rounded.Settings,
                            title = "Settings",
                            testTag = "profile_link_settings",
                            onClick = onOpenSettings
                        )
                        ProfileLinkRow(
                            icon = Icons.AutoMirrored.Rounded.HelpOutline,
                            title = "Help",
                            testTag = "profile_link_help",
                            onClick = { showHelp = true }
                        )
                        ProfileLinkRow(
                            icon = Icons.Rounded.Info,
                            title = "About Game",
                            testTag = "profile_link_about",
                            onClick = { showAbout = true }
                        )
                        ProfileLinkRow(
                            icon = Icons.Rounded.Security,
                            title = "Privacy Rights",
                            testTag = "profile_link_privacy_rights",
                            onClick = { showPrivacyRights = true }
                        )
                        ProfileLinkRow(
                            icon = Icons.Rounded.Tune,
                            title = "Privacy Preferences",
                            testTag = "profile_link_privacy_prefs",
                            onClick = { showPrivacyPrefs = true }
                        )
                    }
                }
            }
        }
    }

    if (showHelp) HelpDialog(onDismiss = { showHelp = false })
    if (showAbout) AboutGameDialog(onDismiss = { showAbout = false })
    if (showPrivacyRights) PrivacyRightsDialog(onDismiss = { showPrivacyRights = false })
    if (showPrivacyPrefs) {
        PrivacyPreferencesDialog(
            analyticsConsent = summary.settings.analyticsConsent,
            personalizedTipsConsent = summary.settings.personalizedTipsConsent,
            onSave = onUpdatePrivacyPrefs,
            onDismiss = { showPrivacyPrefs = false }
        )
    }
    if (showEditName) {
        EditPlayerNameDialog(
            currentName = summary.settings.playerName,
            onSave = onUpdatePlayerName,
            onDismiss = { showEditName = false }
        )
    }
}

@Composable
private fun ProfileStatBox(
    label: String,
    value: String,
    icon: ImageVector,
    tint: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = PureWhite),
        border = BorderStroke(1.dp, SoftBorderBlue)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(imageVector = icon, contentDescription = label, tint = tint, modifier = Modifier.size(24.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                color = CobaltDark
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ProfileLinkRow(
    icon: ImageVector,
    title: String,
    testTag: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        color = PureWhite,
        modifier = Modifier
            .fillMaxWidth()
            .testTag(testTag)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = CobaltPrimary)
            Spacer(modifier = Modifier.width(14.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = CobaltDark,
                modifier = Modifier.weight(1f)
            )
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
