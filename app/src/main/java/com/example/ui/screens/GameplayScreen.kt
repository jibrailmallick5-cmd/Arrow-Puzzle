package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
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
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.Lightbulb
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.Straighten
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.domain.GameMode
import com.example.domain.GraphicsQuality
import com.example.domain.LocalizedStrings
import com.example.ui.components.ArrowBoard3D
import com.example.ui.components.CompletedPuzzlePreview
import com.example.ui.components.ConfettiOverlay
import com.example.ui.components.HeartsBar
import com.example.ui.theme.AmberHint
import com.example.ui.theme.AzureBright
import com.example.ui.theme.CobaltDark
import com.example.ui.theme.CobaltPrimary
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.GoldContainer
import com.example.ui.theme.HeartCrimson
import com.example.ui.theme.HeartCrimsonLight
import com.example.ui.theme.IceBlueContainer
import com.example.ui.theme.IceSurface
import com.example.ui.theme.PureWhite
import com.example.ui.theme.SoftBorderBlue
import com.example.ui.viewmodel.GameplayStatus
import com.example.ui.viewmodel.GameplayUiState
import com.example.ui.viewmodel.TutorialStep
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun GameplayScreen(
    uiState: GameplayUiState,
    hintBalance: Int,
    graphicsQuality: GraphicsQuality,
    strings: LocalizedStrings,
    onArrowTapped: (com.example.domain.ArrowTile) -> Unit,
    onUseHint: () -> Unit,
    onToggleRuler: () -> Unit,
    onRestart: () -> Unit,
    onSkipTutorial: () -> Unit,
    onNextLevel: () -> Unit,
    onBackToMenu: () -> Unit
) {
    BackHandler { onBackToMenu() }

    val level = uiState.level
    val isTutorial = level.mode == GameMode.TUTORIAL

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(PureWhite)
            .windowInsetsPadding(WindowInsets.safeDrawing),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 600.dp)
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top HUD Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = CircleShape,
                        color = IceSurface,
                        border = BorderStroke(1.dp, SoftBorderBlue)
                    ) {
                        IconButton(
                            onClick = onBackToMenu,
                            modifier = Modifier.testTag("gameplay_back_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                                contentDescription = strings.mainMenu,
                                tint = CobaltPrimary
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (level.mode == GameMode.CAMPAIGN) strings.appTitle else level.title,
                            style = MaterialTheme.typography.titleLarge,
                            color = CobaltDark,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (level.mode == GameMode.CAMPAIGN) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.padding(top = 2.dp)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = IceBlueContainer,
                                    border = BorderStroke(1.dp, SoftBorderBlue)
                                ) {
                                    Text(
                                        text = "Level ${level.levelNumber}",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = CobaltPrimary,
                                        maxLines = 1,
                                        modifier = Modifier
                                            .padding(horizontal = 8.dp, vertical = 2.dp)
                                            .testTag("current_running_level_text")
                                    )
                                }
                                Text(
                                    text = "${level.patternTag} · ${uiState.remainingArrows.size} left",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        } else {
                            Text(
                                text = "${level.difficulty.title} · ${uiState.remainingArrows.size} left",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                if (isTutorial) {
                    TextButton(
                        onClick = onSkipTutorial,
                        modifier = Modifier.testTag("skip_tutorial_button")
                    ) {
                        Text(
                            text = strings.skipTutorial,
                            color = CobaltPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else {
                    HeartsBar(
                        hearts = uiState.hearts,
                        maxHearts = uiState.maxHearts,
                        modifier = Modifier.testTag("hearts_bar")
                    )
                }
            }

            // Tutorial Guided Banner OR Mode Tip
            if (isTutorial && uiState.status == GameplayStatus.PLAYING) {
                Spacer(modifier = Modifier.height(10.dp))
                TutorialInstructionCard(
                    step = uiState.tutorialStep,
                    strings = strings
                )
            } else if (uiState.isRulerActive) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = IceBlueContainer,
                    border = BorderStroke(1.dp, SoftBorderBlue)
                ) {
                    Text(
                        text = "Ruler Active — Green lines are clear to escape; Red X marks blockers.",
                        style = MaterialTheme.typography.labelMedium,
                        color = CobaltDark,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 3D Interactive Arrow Board
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                ArrowBoard3D(
                    levelId = level.id,
                    rows = level.rows,
                    cols = level.cols,
                    remainingArrows = uiState.remainingArrows,
                    escapingArrows = uiState.escapingArrows,
                    hintedArrowId = uiState.hintedArrowId,
                    isRulerActive = uiState.isRulerActive,
                    allInspections = uiState.allInspections,
                    blockedBump = uiState.blockedBump,
                    graphicsQuality = graphicsQuality,
                    onArrowTapped = onArrowTapped,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(4.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Bottom Tool Dock: Restart, Hint (Lightbulb + Counter), Ruler (Straighten)
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                color = IceSurface,
                border = BorderStroke(1.dp, SoftBorderBlue)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Restart Button
                    FilledTonalButton(
                        onClick = onRestart,
                        modifier = Modifier
                            .height(48.dp)
                            .testTag("restart_button"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = PureWhite,
                            contentColor = CobaltPrimary
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Refresh,
                            contentDescription = "Restart level"
                        )
                    }

                    // Hint Button with Lightbulb Icon + Counter
                    Button(
                        onClick = onUseHint,
                        enabled = isTutorial || hintBalance > 0,
                        modifier = Modifier
                            .height(48.dp)
                            .testTag("hint_button"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AmberHint,
                            contentColor = PureWhite
                        )
                    ) {
                        BadgedBox(
                            badge = {
                                if (!isTutorial) {
                                    Badge(
                                        containerColor = CobaltDark,
                                        contentColor = PureWhite,
                                        modifier = Modifier.testTag("hint_counter_badge")
                                    ) {
                                        Text("$hintBalance")
                                    }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Lightbulb,
                                contentDescription = strings.hint
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(strings.hint, fontWeight = FontWeight.Bold)
                    }

                    // Ruler Helper Tool Button
                    FilledTonalButton(
                        onClick = onToggleRuler,
                        modifier = Modifier
                            .height(48.dp)
                            .testTag("ruler_button"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = if (uiState.isRulerActive) CobaltPrimary else PureWhite,
                            contentColor = if (uiState.isRulerActive) PureWhite else CobaltPrimary
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Straighten,
                            contentDescription = strings.ruler
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(strings.ruler, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
        }

        // Level Completed & Confetti Sunburst Overlay
        AnimatedVisibility(
            visible = uiState.status == GameplayStatus.LEVEL_COMPLETED,
            enter = fadeIn() + scaleIn(initialScale = 0.94f),
            exit = fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color(0xFF0A84FF), AzureBright, CobaltPrimary)
                        )
                    )
                    .testTag("level_completed_card"),
                contentAlignment = Alignment.Center
            ) {
                // Radiant Blue Sunburst Rays
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val center = Offset(size.width / 2f, size.height * 0.45f)
                    val radius = size.maxDimension * 1.1f
                    val rayCount = 16
                    val sliceDeg = 360f / rayCount
                    for (i in 0 until rayCount step 2) {
                        val a1 = Math.toRadians((i * sliceDeg).toDouble())
                        val a2 = Math.toRadians(((i + 1) * sliceDeg).toDouble())
                        val path = Path().apply {
                            moveTo(center.x, center.y)
                            lineTo(center.x + (cos(a1) * radius).toFloat(), center.y + (sin(a1) * radius).toFloat())
                            lineTo(center.x + (cos(a2) * radius).toFloat(), center.y + (sin(a2) * radius).toFloat())
                            close()
                        }
                        drawPath(path = path, color = PureWhite.copy(alpha = 0.08f))
                    }
                }

                ConfettiOverlay(graphicsQuality = graphicsQuality)

                Column(
                    modifier = Modifier
                        .widthIn(max = 420.dp)
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 24.dp, vertical = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (isTutorial) strings.tutorialCompleted else strings.levelCompleted,
                        style = MaterialTheme.typography.displaySmall,
                        color = PureWhite,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        for (s in 1..3) {
                            Icon(
                                imageVector = Icons.Rounded.Star,
                                contentDescription = "Star $s",
                                tint = if (s <= uiState.earnedStars) GoldAccent else PureWhite.copy(alpha = 0.28f),
                                modifier = Modifier.size(30.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Crisp White Completed Puzzle Preview Card (matching reference screenshot)
                    Card(
                        shape = RoundedCornerShape(26.dp),
                        colors = CardDefaults.cardColors(containerColor = PureWhite),
                        elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
                    ) {
                        CompletedPuzzlePreview(
                            rows = level.rows,
                            cols = level.cols,
                            initialArrows = uiState.initialArrows,
                            modifier = Modifier
                                .size(210.dp)
                                .padding(10.dp)
                                .testTag("completed_puzzle_preview")
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    if (uiState.earnedBonusHints > 0 || uiState.earnedTournamentScore > 0) {
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = GoldContainer
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (uiState.earnedBonusHints > 0) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            Icons.Rounded.Lightbulb,
                                            contentDescription = null,
                                            tint = AmberHint,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Text(
                                            " +${uiState.earnedBonusHints} Hints",
                                            style = MaterialTheme.typography.labelLarge,
                                            color = CobaltDark
                                        )
                                    }
                                }
                                if (uiState.earnedTournamentScore > 0) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            Icons.Rounded.EmojiEvents,
                                            contentDescription = null,
                                            tint = GoldAccent,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Text(
                                            " +${uiState.earnedTournamentScore} pts",
                                            style = MaterialTheme.typography.labelLarge,
                                            color = CobaltDark
                                        )
                                    }
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                    }

                    val nextButtonLabel = when {
                        isTutorial -> strings.newGame
                        level.mode == GameMode.TOURNAMENT && (level.tournamentStage ?: 1) < 5 -> "Next Stage"
                        level.mode == GameMode.TOURNAMENT -> "View Standings"
                        level.mode == GameMode.DAILY_CHALLENGE -> "Back to Calendar"
                        else -> strings.newGame
                    }
                    val nextCampaignLevel = when {
                        isTutorial -> 1
                        level.mode == GameMode.CAMPAIGN -> level.levelNumber + 1
                        else -> null
                    }

                    Button(
                        onClick = onNextLevel,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                            .testTag("next_level_button"),
                        shape = RoundedCornerShape(50),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PureWhite,
                            contentColor = CobaltPrimary
                        ),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp)
                    ) {
                        Icon(Icons.Rounded.PlayArrow, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            nextButtonLabel,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        if (nextCampaignLevel != null) {
                            Spacer(modifier = Modifier.width(10.dp))
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = IceBlueContainer,
                                border = BorderStroke(1.dp, SoftBorderBlue)
                            ) {
                                Text(
                                    text = "Level $nextCampaignLevel",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = CobaltPrimary,
                                    modifier = Modifier
                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                        .testTag("next_level_badge")
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    TextButton(
                        onClick = onBackToMenu,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("win_main_menu_button")
                    ) {
                        Text(
                            strings.mainMenu,
                            style = MaterialTheme.typography.titleMedium,
                            color = PureWhite,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Game Over Overlay
        AnimatedVisibility(
            visible = uiState.status == GameplayStatus.GAME_OVER,
            enter = fadeIn() + scaleIn(initialScale = 0.92f),
            exit = fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0x990F172A)),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier
                        .widthIn(max = 390.dp)
                        .fillMaxWidth()
                        .padding(24.dp)
                        .testTag("game_over_card"),
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(containerColor = PureWhite)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = HeartCrimsonLight,
                            modifier = Modifier.size(56.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Rounded.Warning,
                                    contentDescription = null,
                                    tint = HeartCrimson,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = strings.gameOver,
                            style = MaterialTheme.typography.headlineMedium,
                            color = HeartCrimson,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Watch out for blocking arrows! Use the Ruler or Hint tool if you get stuck.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        Button(
                            onClick = onRestart,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("retry_level_button"),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = CobaltPrimary)
                        ) {
                            Icon(Icons.Rounded.Refresh, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(strings.retryLevel, style = MaterialTheme.typography.titleMedium)
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedButton(
                            onClick = onBackToMenu,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("gameover_main_menu_button"),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text(strings.mainMenu)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TutorialInstructionCard(
    step: TutorialStep,
    strings: LocalizedStrings
) {
    val (headline, body) = when (step) {
        TutorialStep.TAP_FIRST_ARROW ->
            strings.tutorialWelcome to strings.tutorialTapFirst
        TutorialStep.EXPLAIN_BLOCKED_AND_SELECT_CLEAR ->
            "Great Move!" to strings.tutorialBlockedInfo
        TutorialStep.FINAL_CLEAR_ARROW ->
            "Almost Done!" to strings.tutorialSelectClear
        TutorialStep.COMPLETED ->
            strings.tutorialCompleted to "Get ready for unlimited puzzles!"
        TutorialStep.NONE -> "" to ""
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("tutorial_instruction_card"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = IceBlueContainer),
        border = BorderStroke(1.2.dp, CobaltPrimary.copy(alpha = 0.35f))
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = headline,
                style = MaterialTheme.typography.titleMedium,
                color = CobaltDark
            )
            Text(
                text = body,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
    }
}
