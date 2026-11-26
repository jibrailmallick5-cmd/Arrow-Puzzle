package com.example.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.Lightbulb
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.PlayerStatsSummary
import com.example.domain.LocalizedStrings
import com.example.ui.theme.AmberHint
import com.example.ui.theme.CobaltDark
import com.example.ui.theme.CobaltPrimary
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.IceBlueContainer
import com.example.ui.theme.IceSurface
import com.example.ui.theme.PureWhite
import com.example.ui.theme.SoftBorderBlue

@Composable
fun MainMenuScreen(
    summary: PlayerStatsSummary,
    strings: LocalizedStrings,
    onPlayCurrentLevel: () -> Unit,
    onOpenDailyChallenge: () -> Unit,
    onOpenTournament: () -> Unit,
    onOpenProfile: () -> Unit,
    onOpenSettings: () -> Unit
) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }
    val contentAlpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(320),
        label = "menu_fade"
    )

    val unlockedLevel = summary.settings.unlockedLevel

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(PureWhite, IceSurface, PureWhite)
                )
            )
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .alpha(contentAlpha),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 560.dp)
                .fillMaxSize()
        ) {
            // Top Utility Bar (Stars, Trophies, Hints + Settings) - pinned at top
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    StatPill(
                        icon = Icons.Rounded.Star,
                        value = summary.totalStars.toString(),
                        tint = GoldAccent,
                        contentDesc = "Total stars"
                    )
                    StatPill(
                        icon = Icons.Rounded.EmojiEvents,
                        value = summary.totalTrophies.toString(),
                        tint = CobaltPrimary,
                        contentDesc = "Total trophies"
                    )
                    StatPill(
                        icon = Icons.Rounded.Lightbulb,
                        value = summary.settings.hintBalance.toString(),
                        tint = AmberHint,
                        contentDesc = "Hints available"
                    )
                }

                Surface(
                    shape = CircleShape,
                    color = PureWhite,
                    border = BorderStroke(1.dp, SoftBorderBlue),
                    shadowElevation = 2.dp
                ) {
                    IconButton(
                        onClick = onOpenSettings,
                        modifier = Modifier.testTag("menu_settings_button")
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Settings,
                            contentDescription = strings.settings,
                            tint = CobaltPrimary
                        )
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

            // Game Logo Card (User-provided Arrow Emblem)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = PureWhite),
                border = BorderStroke(1.5.dp, SoftBorderBlue),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_hero_arrow_emblem),
                        contentDescription = strings.appTitle,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(172.dp)
                            .clip(RoundedCornerShape(16.dp)),
                        contentScale = ContentScale.Fit
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Primary Action: Play Now + Current Running Level
            Button(
                onClick = onPlayCurrentLevel,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .testTag("menu_new_game_button"),
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = CobaltPrimary,
                    contentColor = PureWhite
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.PlayArrow,
                    contentDescription = null,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = strings.newGame,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(10.dp))
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = PureWhite.copy(alpha = 0.22f),
                    border = BorderStroke(1.dp, PureWhite.copy(alpha = 0.45f))
                ) {
                    Text(
                        text = "Level $unlockedLevel",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = PureWhite,
                        modifier = Modifier
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                            .testTag("menu_play_level_badge")
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Feature Grid: Daily Challenge + Tournament
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MenuFeatureCard(
                    title = strings.dailyChallenge,
                    subtitle = "${summary.dailyCompletedCount} Completed",
                    icon = Icons.Rounded.CalendarMonth,
                    accentColor = CobaltPrimary,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("menu_daily_button"),
                    onClick = onOpenDailyChallenge
                )

                MenuFeatureCard(
                    title = strings.tournament,
                    subtitle = "Arena Blitz",
                    icon = Icons.Rounded.EmojiEvents,
                    accentColor = GoldAccent,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("menu_tournament_button"),
                    onClick = onOpenTournament
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Bottom Row: Player Profile & Settings
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MenuFeatureCard(
                    title = strings.profile,
                    subtitle = summary.rankTitle,
                    icon = Icons.Rounded.Person,
                    accentColor = CobaltPrimary,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("menu_profile_button"),
                    onClick = onOpenProfile
                )

                MenuFeatureCard(
                    title = strings.settings,
                    subtitle = summary.settings.language.displayName,
                    icon = Icons.Rounded.Settings,
                    accentColor = CobaltPrimary,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("menu_settings_card"),
                    onClick = onOpenSettings
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun StatPill(
    icon: ImageVector,
    value: String,
    tint: Color,
    contentDesc: String
) {
    Surface(
        shape = RoundedCornerShape(50),
        color = PureWhite,
        border = BorderStroke(1.dp, SoftBorderBlue),
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = contentDesc,
                tint = tint,
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = value,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
    }
}

@Composable
private fun MenuFeatureCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    accentColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = PureWhite),
        border = BorderStroke(1.2.dp, SoftBorderBlue),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = IceBlueContainer,
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = accentColor,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground,
                maxLines = 1
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
        }
    }
}
