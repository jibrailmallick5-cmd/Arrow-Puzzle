package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun HelpDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("How to Play Arrow Puzzle", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("1. Tap any arrow whose path in its pointing direction is completely clear to the edge of the board.")
                Text("2. If another arrow sits anywhere along its path, it is blocked and cannot escape. Tapping a blocked arrow costs 1 red heart.")
                Text("3. Use the Lightbulb Hint to highlight a guaranteed safe move, or toggle the Ruler tool to inspect exact laser sightlines and grid coordinates.")
                Text("4. Clear all arrows before losing all 3 hearts to earn up to 3 Stars!")
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Got It") }
        }
    )
}

@Composable
fun AboutGameDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("About Arrow Puzzle", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Arrow Puzzle — 3D Escape Edition", fontWeight = FontWeight.SemiBold)
                Text("Version 1.0.0")
                Text("Features 1,000+ progressively challenging, solver-verified puzzles across 6 difficulty tiers, Daily Calendar Challenges, Offline Arena Tournaments, and custom 3D lighting & audio synthesis.")
                Text("All game progress, achievements, and preferences are stored safely on your device for uninterrupted offline play.")
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        }
    )
}

@Composable
fun PrivacyRightsDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Privacy Rights", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Your Data, Your Control", fontWeight = FontWeight.SemiBold)
                Text("• Local-First Storage: Your level completions, daily calendar stars, tournament trophies, and preferences stay exclusively on your device.")
                Text("• Right to Erasure: You can wipe all saved game progress at any time via Settings → Reset Progress.")
                Text("• No Hidden Tracking: Arrow Puzzle requires zero account sign-in and never accesses personal contacts, location, or media.")
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Understood") }
        }
    )
}

@Composable
fun PrivacyPreferencesDialog(
    analyticsConsent: Boolean,
    personalizedTipsConsent: Boolean,
    onSave: (Boolean, Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    var analytics by remember { mutableStateOf(analyticsConsent) }
    var tips by remember { mutableStateOf(personalizedTipsConsent) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Privacy Preferences", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Local Puzzle Insights", style = MaterialTheme.typography.titleMedium)
                        Text(
                            "Track solve times & streaks locally for your Profile stats.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = analytics,
                        onCheckedChange = { analytics = it },
                        modifier = Modifier.testTag("privacy_analytics_switch")
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Smart Gameplay Tips", style = MaterialTheme.typography.titleMedium)
                        Text(
                            "Show helpful strategy tips on harder tiers.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = tips,
                        onCheckedChange = { tips = it },
                        modifier = Modifier.testTag("privacy_tips_switch")
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onSave(analytics, tips)
                onDismiss()
            }) { Text("Save") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun EditPlayerNameDialog(
    currentName: String,
    onSave: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf(currentName) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Player Name", fontWeight = FontWeight.Bold) },
        text = {
            Column {
                Text("Shown on your Profile & Tournament Leaderboard:")
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = name,
                    onValueChange = { if (it.length <= 20) name = it },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onSave(name)
                onDismiss()
            }) { Text("Save") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
