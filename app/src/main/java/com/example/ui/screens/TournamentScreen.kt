package com.example.ui.screens

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
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Timer
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
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.GameRepository
import com.example.data.TournamentEntryEntity
import com.example.domain.DateHelper
import com.example.ui.theme.CobaltDark
import com.example.ui.theme.CobaltPrimary
import com.example.ui.theme.EmeraldContainer
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.GoldContainer
import com.example.ui.theme.IceBlueContainer
import com.example.ui.theme.IceSurface
import com.example.ui.theme.MutedSlate
import com.example.ui.theme.PureWhite
import com.example.ui.theme.SoftBorderBlue
import kotlinx.coroutines.delay

@Composable
fun TournamentScreen(
    repository: GameRepository,
    playerName: String,
    entry: TournamentEntryEntity?,
    onPlayStage: (eventId: Int, stageIndex: Int) -> Unit,
    onBack: () -> Unit
) {
    val eventId = remember { DateHelper.currentTournamentEventId() }
    var remainingSec by remember { mutableLongStateOf(DateHelper.remainingSecondsInCurrentTournament()) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(1000L)
            remainingSec = (remainingSec - 1L).coerceAtLeast(0L)
        }
    }

    val playerScore = entry?.bestScore ?: 0
    val stagesCompleted = entry?.stageReached ?: 0
    val nextStage = (stagesCompleted + 1).coerceAtMost(5)
    val isEventCompleted = entry?.completedEvent == true
    val leaderboard = remember(playerName, playerScore) {
        repository.buildTournamentLeaderboard(playerName, playerScore)
    }
    val playerRank = leaderboard.firstOrNull { it.isPlayer }?.rank ?: 9

    val daysLeft = remainingSec / 86400
    val hoursLeft = (remainingSec % 86400) / 3600
    val minsLeft = (remainingSec % 3600) / 60
    val secsLeft = remainingSec % 60
    val countdownText = "${daysLeft}d ${hoursLeft}h ${minsLeft}m ${secsLeft}s"

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
                    modifier = Modifier.testTag("tournament_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = "Back",
                        tint = CobaltPrimary
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Arena Blitz Tournament",
                        style = MaterialTheme.typography.headlineSmall,
                        color = CobaltDark
                    )
                    Text(
                        text = "Weekly Offline Cup #$eventId",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
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
                // Event Hero & Countdown Banner
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = CobaltPrimary),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.img_trophy_sapphire_gold),
                                contentDescription = "Tournament Cup",
                                modifier = Modifier
                                    .size(72.dp)
                                    .clip(RoundedCornerShape(18.dp)),
                                contentScale = ContentScale.Crop
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (isEventCompleted) "Tournament Completed!" else "5-Stage Speed & Logic Cup",
                                    style = MaterialTheme.typography.titleLarge,
                                    color = PureWhite
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Rounded.Timer,
                                        contentDescription = null,
                                        tint = GoldAccent,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = " Ends in $countdownText",
                                        style = MaterialTheme.typography.labelLarge,
                                        color = IceBlueContainer
                                    )
                                }
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            TournamentMetricPill("Your Score", "$playerScore", Modifier.weight(1f))
                            TournamentMetricPill("Live Rank", "#$playerRank", Modifier.weight(1f))
                            TournamentMetricPill("Trophies", "${entry?.trophiesEarned ?: 0}/3", Modifier.weight(1f))
                        }

                        Button(
                            onClick = { onPlayStage(eventId, if (isEventCompleted) 5 else nextStage) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("play_tournament_button"),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = PureWhite,
                                contentColor = CobaltDark
                            )
                        ) {
                            Icon(Icons.Rounded.PlayArrow, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isEventCompleted) {
                                    "Replay Stage 5 for Higher Score"
                                } else {
                                    "Play Stage $nextStage of 5"
                                },
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // 5-Stage Gauntlet Selector
                Card(
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = PureWhite),
                    border = BorderStroke(1.dp, SoftBorderBlue)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "Tournament Stages & Rewards",
                            style = MaterialTheme.typography.titleMedium,
                            color = CobaltDark
                        )
                        for (stage in 1..5) {
                            val unlocked = stage <= stagesCompleted + 1
                            val cleared = stage <= stagesCompleted
                            val rewardDesc = when (stage) {
                                3 -> "Reward: 1 Trophy + 1 Hint"
                                5 -> "Grand Prize: 3 Trophies + 3 Hints"
                                else -> "Score Multiplier x$stage"
                            }

                            Surface(
                                onClick = { if (unlocked) onPlayStage(eventId, stage) },
                                enabled = unlocked,
                                shape = RoundedCornerShape(14.dp),
                                color = if (cleared) EmeraldContainer else IceSurface,
                                border = BorderStroke(1.dp, SoftBorderBlue)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text(
                                            text = "Stage $stage",
                                            style = MaterialTheme.typography.titleMedium,
                                            color = if (unlocked) CobaltDark else MutedSlate
                                        )
                                        Text(
                                            text = rewardDesc,
                                            style = MaterialTheme.typography.labelMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    when {
                                        cleared -> Icon(
                                            Icons.Rounded.CheckCircle,
                                            contentDescription = "Cleared",
                                            tint = EmeraldSuccess
                                        )
                                        unlocked -> Icon(
                                            Icons.Rounded.PlayArrow,
                                            contentDescription = "Play",
                                            tint = CobaltPrimary
                                        )
                                        else -> Icon(
                                            Icons.Rounded.Lock,
                                            contentDescription = "Locked",
                                            tint = MutedSlate
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Offline Ranking Leaderboard
                Card(
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = PureWhite),
                    border = BorderStroke(1.dp, SoftBorderBlue)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Rounded.EmojiEvents, contentDescription = null, tint = GoldAccent)
                            Text(
                                text = "Arena Standings",
                                style = MaterialTheme.typography.titleMedium,
                                color = CobaltDark
                            )
                        }

                        leaderboard.forEach { row ->
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (row.isPlayer) IceBlueContainer else PureWhite,
                                border = if (row.isPlayer) BorderStroke(1.5.dp, CobaltPrimary) else null
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        shape = CircleShape,
                                        color = if (row.rank <= 3) GoldContainer else IceSurface,
                                        modifier = Modifier.size(30.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(
                                                text = "#${row.rank}",
                                                style = MaterialTheme.typography.labelMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = CobaltDark
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = if (row.isPlayer) "${row.name} (You)" else row.name,
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = if (row.isPlayer) FontWeight.Bold else FontWeight.Normal,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Text(
                                        text = "${row.score} pts",
                                        style = MaterialTheme.typography.labelLarge,
                                        color = CobaltPrimary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TournamentMetricPill(label: String, value: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = PureWhite.copy(alpha = 0.16f)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = PureWhite
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = IceBlueContainer
            )
        }
    }
}
