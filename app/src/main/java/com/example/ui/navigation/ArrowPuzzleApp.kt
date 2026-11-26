package com.example.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.domain.GameMode
import com.example.ui.screens.DailyChallengeScreen
import com.example.ui.screens.GameplayScreen
import com.example.ui.screens.MainMenuScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.TournamentScreen
import com.example.ui.theme.CobaltPrimary
import com.example.ui.theme.PureWhite
import com.example.ui.viewmodel.AppViewModel
import com.example.ui.viewmodel.GameViewModel

object NavRoutes {
    const val MENU = "menu"
    const val GAMEPLAY = "gameplay"
    const val DAILY = "daily"
    const val TOURNAMENT = "tournament"
    const val PROFILE = "profile"
    const val SETTINGS = "settings"
}

@Composable
fun ArrowPuzzleApp(
    appViewModel: AppViewModel
) {
    val isReady by appViewModel.isReady.collectAsStateWithLifecycle()
    val summary by appViewModel.playerSummary.collectAsStateWithLifecycle()
    val strings by appViewModel.localizedStrings.collectAsStateWithLifecycle()
    val dailyMap by appViewModel.currentMonthDailyMap.collectAsStateWithLifecycle()
    val tournamentEntry by appViewModel.currentTournamentEntry.collectAsStateWithLifecycle()

    if (!isReady) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(PureWhite),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = CobaltPrimary)
        }
        return
    }

    val gameViewModel: GameViewModel = viewModel(
        factory = object : androidx.lifecycle.ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                return GameViewModel(appViewModel.repository, appViewModel.soundEngine) as T
            }
        }
    )
    val gameplayState by gameViewModel.uiState.collectAsStateWithLifecycle()

    val navController = rememberNavController()
    val startDest = remember {
        if (!summary.settings.tutorialCompleted) {
            gameViewModel.startTutorial()
            NavRoutes.GAMEPLAY
        } else {
            NavRoutes.MENU
        }
    }

    NavHost(
        navController = navController,
        startDestination = startDest,
        enterTransition = {
            fadeIn(animationSpec = tween(220)) +
                slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Start, tween(240))
        },
        exitTransition = {
            fadeOut(animationSpec = tween(200))
        },
        popEnterTransition = {
            fadeIn(animationSpec = tween(220))
        },
        popExitTransition = {
            fadeOut(animationSpec = tween(200)) +
                slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.End, tween(240))
        }
    ) {
        composable(NavRoutes.MENU) {
            MainMenuScreen(
                summary = summary,
                strings = strings,
                onPlayCurrentLevel = {
                    appViewModel.soundEngine.playButtonClick()
                    gameViewModel.startCampaignLevel(summary.settings.unlockedLevel)
                    navController.navigate(NavRoutes.GAMEPLAY)
                },
                onOpenDailyChallenge = {
                    appViewModel.soundEngine.playButtonClick()
                    navController.navigate(NavRoutes.DAILY)
                },
                onOpenTournament = {
                    appViewModel.soundEngine.playButtonClick()
                    navController.navigate(NavRoutes.TOURNAMENT)
                },
                onOpenProfile = {
                    appViewModel.soundEngine.playButtonClick()
                    navController.navigate(NavRoutes.PROFILE)
                },
                onOpenSettings = {
                    appViewModel.soundEngine.playButtonClick()
                    navController.navigate(NavRoutes.SETTINGS)
                }
            )
        }

        composable(NavRoutes.DAILY) {
            DailyChallengeScreen(
                dailyCompletions = dailyMap,
                onPlayDay = { epochDay, dom ->
                    appViewModel.soundEngine.playButtonClick()
                    gameViewModel.startDailyChallenge(epochDay, dom)
                    navController.navigate(NavRoutes.GAMEPLAY)
                },
                onBack = {
                    appViewModel.soundEngine.playButtonClick()
                    navController.popBackStack()
                }
            )
        }

        composable(NavRoutes.TOURNAMENT) {
            TournamentScreen(
                repository = appViewModel.repository,
                playerName = summary.settings.playerName,
                entry = tournamentEntry,
                onPlayStage = { eventId, stage ->
                    appViewModel.soundEngine.playButtonClick()
                    gameViewModel.startTournamentStage(eventId, stage)
                    navController.navigate(NavRoutes.GAMEPLAY)
                },
                onBack = {
                    appViewModel.soundEngine.playButtonClick()
                    navController.popBackStack()
                }
            )
        }

        composable(NavRoutes.PROFILE) {
            ProfileScreen(
                summary = summary,
                onOpenSettings = {
                    appViewModel.soundEngine.playButtonClick()
                    navController.navigate(NavRoutes.SETTINGS)
                },
                onUpdatePlayerName = { name ->
                    appViewModel.setPlayerName(name)
                },
                onUpdatePrivacyPrefs = { analytics, tips ->
                    appViewModel.setPrivacyPreferences(analytics, tips)
                },
                onBack = {
                    appViewModel.soundEngine.playButtonClick()
                    navController.popBackStack()
                }
            )
        }

        composable(NavRoutes.SETTINGS) {
            SettingsScreen(
                settings = summary.settings,
                onMusicToggle = { appViewModel.setMusicEnabled(it) },
                onSfxToggle = { appViewModel.setSoundEffectsEnabled(it) },
                onVibrationToggle = { appViewModel.setVibrationEnabled(it) },
                onLanguageSelected = { appViewModel.setLanguage(it) },
                onGraphicsSelected = { appViewModel.setGraphicsQuality(it) },
                onReplayTutorial = {
                    appViewModel.soundEngine.playButtonClick()
                    gameViewModel.startTutorial()
                    navController.navigate(NavRoutes.GAMEPLAY)
                },
                onResetProgress = {
                    appViewModel.soundEngine.playButtonClick()
                    appViewModel.resetProgress()
                },
                onBack = {
                    appViewModel.soundEngine.playButtonClick()
                    navController.popBackStack()
                }
            )
        }

        composable(NavRoutes.GAMEPLAY) {
            GameplayScreen(
                uiState = gameplayState,
                hintBalance = summary.settings.hintBalance,
                graphicsQuality = summary.settings.graphicsQuality,
                strings = strings,
                onArrowTapped = { arrow -> gameViewModel.onArrowTapped(arrow) },
                onUseHint = { gameViewModel.useHint() },
                onToggleRuler = { gameViewModel.toggleRulerTool() },
                onRestart = {
                    appViewModel.soundEngine.playButtonClick()
                    gameViewModel.restartCurrentLevel()
                },
                onSkipTutorial = {
                    gameViewModel.skipTutorial {
                        if (!navController.popBackStack(NavRoutes.MENU, inclusive = false)) {
                            navController.navigate(NavRoutes.MENU) {
                                popUpTo(NavRoutes.GAMEPLAY) { inclusive = true }
                            }
                        }
                    }
                },
                onNextLevel = {
                    appViewModel.soundEngine.playButtonClick()
                    val current = gameplayState.level
                    when (current.mode) {
                        GameMode.TUTORIAL -> {
                            gameViewModel.startCampaignLevel(1)
                        }
                        GameMode.CAMPAIGN -> {
                            gameViewModel.startCampaignLevel(current.levelNumber + 1)
                        }
                        GameMode.DAILY_CHALLENGE -> {
                            navController.popBackStack()
                        }
                        GameMode.TOURNAMENT -> {
                            val stage = current.tournamentStage ?: 1
                            if (stage < 5) {
                                val eventId = com.example.domain.DateHelper.currentTournamentEventId()
                                gameViewModel.startTournamentStage(eventId, stage + 1)
                            } else {
                                navController.popBackStack()
                            }
                        }
                    }
                },
                onBackToMenu = {
                    appViewModel.soundEngine.playButtonClick()
                    if (!navController.popBackStack(NavRoutes.MENU, inclusive = false)) {
                        navController.navigate(NavRoutes.MENU) {
                            popUpTo(NavRoutes.GAMEPLAY) { inclusive = true }
                        }
                    }
                }
            )
        }
    }
}
