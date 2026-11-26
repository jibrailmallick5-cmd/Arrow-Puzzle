package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.SoundEngine
import com.example.data.GameRepository
import com.example.domain.ArrowTile
import com.example.domain.DateHelper
import com.example.domain.GameMode
import com.example.domain.GridCell
import com.example.domain.PathInspection
import com.example.domain.PuzzleGenerator
import com.example.domain.PuzzleLevel
import com.example.domain.PuzzleSolver
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class TutorialStep {
    NONE,
    TAP_FIRST_ARROW,
    EXPLAIN_BLOCKED_AND_SELECT_CLEAR,
    FINAL_CLEAR_ARROW,
    COMPLETED
}

enum class GameplayStatus {
    PLAYING,
    LEVEL_COMPLETED,
    GAME_OVER
}

data class BlockedBumpFeedback(
    val arrowId: Int,
    val blockerCell: GridCell?,
    val rayCells: List<GridCell>,
    val eventSeq: Long
)

data class EscapingArrow(
    val tile: ArrowTile,
    val eventSeq: Long
)

data class GameplayUiState(
    val level: PuzzleLevel = PuzzleGenerator.generateCampaignLevel(1),
    val initialArrows: List<ArrowTile> = emptyList(),
    val remainingArrows: List<ArrowTile> = emptyList(),
    val escapingArrows: List<EscapingArrow> = emptyList(),
    val hearts: Int = 3,
    val maxHearts: Int = 3,
    val movesCount: Int = 0,
    val mistakesCount: Int = 0,
    val elapsedSeconds: Int = 0,
    val hintedArrowId: Int? = null,
    val isRulerActive: Boolean = false,
    val allInspections: Map<Int, PathInspection> = emptyMap(),
    val blockedBump: BlockedBumpFeedback? = null,
    val status: GameplayStatus = GameplayStatus.PLAYING,
    val tutorialStep: TutorialStep = TutorialStep.NONE,
    val earnedStars: Int = 0,
    val earnedBonusHints: Int = 0,
    val earnedTournamentScore: Int = 0,
    val tournamentTrophies: Int = 0
)

class GameViewModel(
    private val repository: GameRepository,
    private val soundEngine: SoundEngine
) : ViewModel() {

    private val _uiState = MutableStateFlow(GameplayUiState())
    val uiState: StateFlow<GameplayUiState> = _uiState.asStateFlow()

    private var timerJob: Job? = null
    private var levelTransitionJob: Job? = null
    private var eventCounter = 0L

    fun startTutorial() {
        val level = PuzzleGenerator.getTutorialPuzzle()
        startLevelInternal(level, TutorialStep.TAP_FIRST_ARROW)
    }

    fun startCampaignLevel(levelNumber: Int) {
        val level = PuzzleGenerator.generateCampaignLevel(levelNumber)
        viewModelScope.launch {
            repository.setLastPlayedLevel(levelNumber)
        }
        startLevelInternal(level, TutorialStep.NONE)
    }

    fun startDailyChallenge(epochDay: Long, dayOfMonth: Int) {
        val level = PuzzleGenerator.generateDailyLevel(epochDay, dayOfMonth)
        startLevelInternal(level, TutorialStep.NONE)
    }

    fun startTournamentStage(eventId: Int, stageIndex: Int) {
        val level = PuzzleGenerator.generateTournamentStage(eventId, stageIndex)
        startLevelInternal(level, TutorialStep.NONE)
    }

    fun restartCurrentLevel() {
        val current = _uiState.value.level
        val tutorialStep = if (current.mode == GameMode.TUTORIAL) TutorialStep.TAP_FIRST_ARROW else TutorialStep.NONE
        startLevelInternal(current, tutorialStep)
    }

    private fun startLevelInternal(level: PuzzleLevel, tutorialStep: TutorialStep) {
        timerJob?.cancel()
        levelTransitionJob?.cancel()
        levelTransitionJob = null
        val inspections = computeInspections(level.arrows, level.rows, level.cols)
        val initialHint = if (tutorialStep == TutorialStep.TAP_FIRST_ARROW) 1 else null

        _uiState.value = GameplayUiState(
            level = level,
            initialArrows = level.arrows,
            remainingArrows = level.arrows,
            escapingArrows = emptyList(),
            hearts = 3,
            maxHearts = 3,
            movesCount = 0,
            mistakesCount = 0,
            elapsedSeconds = 0,
            hintedArrowId = initialHint,
            isRulerActive = false,
            allInspections = inspections,
            blockedBump = null,
            status = GameplayStatus.PLAYING,
            tutorialStep = tutorialStep
        )
        startTimer()
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1000L)
                val current = _uiState.value
                if (current.status == GameplayStatus.PLAYING) {
                    _uiState.update { it.copy(elapsedSeconds = it.elapsedSeconds + 1) }
                } else {
                    break
                }
            }
        }
    }

    fun onArrowTapped(arrow: ArrowTile) {
        val state = _uiState.value
        if (state.status != GameplayStatus.PLAYING || levelTransitionJob?.isActive == true) return

        // Tutorial step gating so beginners follow the guided sequence clearly
        if (state.level.mode == GameMode.TUTORIAL) {
            handleTutorialTap(arrow, state)
            return
        }

        val inspection = PuzzleSolver.inspectArrowPath(
            arrow = arrow,
            remainingArrows = state.remainingArrows,
            rows = state.rowsOrDefault(),
            cols = state.colsOrDefault()
        )

        if (inspection.isClear) {
            soundEngine.playArrowSelect()
            soundEngine.playArrowEscape()
            removeArrowWithAnimation(arrow, state)
        } else {
            soundEngine.playBlockedBump()
            eventCounter++
            val newHearts = (state.hearts - 1).coerceAtLeast(0)
            val isGameOver = newHearts == 0

            _uiState.update {
                it.copy(
                    hearts = newHearts,
                    mistakesCount = it.mistakesCount + 1,
                    blockedBump = BlockedBumpFeedback(
                        arrowId = arrow.id,
                        blockerCell = inspection.blockerCell,
                        rayCells = inspection.rayCells,
                        eventSeq = eventCounter
                    ),
                    status = if (isGameOver) GameplayStatus.GAME_OVER else GameplayStatus.PLAYING
                )
            }
            if (isGameOver) {
                timerJob?.cancel()
                soundEngine.playGameOver()
            }
        }
    }

    private fun handleTutorialTap(arrow: ArrowTile, state: GameplayUiState) {
        val inspection = PuzzleSolver.inspectArrowPath(
            arrow = arrow,
            remainingArrows = state.remainingArrows,
            rows = state.level.rows,
            cols = state.level.cols
        )

        when (state.tutorialStep) {
            TutorialStep.TAP_FIRST_ARROW -> {
                if (arrow.id == 1 && inspection.isClear) {
                    soundEngine.playArrowEscape()
                    val nextRemaining = state.remainingArrows.filterNot { it.id == arrow.id }
                    enqueueEscapingArrow(arrow)
                    _uiState.update {
                        it.copy(
                            remainingArrows = nextRemaining,
                            allInspections = computeInspections(nextRemaining, state.level.rows, state.level.cols),
                            movesCount = it.movesCount + 1,
                            hintedArrowId = 3, // Highlight the clear top arrow while explaining blocked arrow 2
                            tutorialStep = TutorialStep.EXPLAIN_BLOCKED_AND_SELECT_CLEAR,
                            blockedBump = null
                        )
                    }
                } else {
                    // Gently prompt to tap the highlighted arrow without losing hearts
                    soundEngine.playBlockedBump()
                    eventCounter++
                    _uiState.update {
                        it.copy(
                            blockedBump = BlockedBumpFeedback(
                                arrowId = arrow.id,
                                blockerCell = inspection.blockerCell,
                                rayCells = inspection.rayCells,
                                eventSeq = eventCounter
                            )
                        )
                    }
                }
            }

            TutorialStep.EXPLAIN_BLOCKED_AND_SELECT_CLEAR,
            TutorialStep.FINAL_CLEAR_ARROW -> {
                if (inspection.isClear) {
                    soundEngine.playArrowEscape()
                    val nextRemaining = state.remainingArrows.filterNot { it.id == arrow.id }
                    enqueueEscapingArrow(arrow)
                    if (nextRemaining.isEmpty()) {
                        _uiState.update {
                            it.copy(
                                remainingArrows = emptyList(),
                                allInspections = emptyMap(),
                                movesCount = it.movesCount + 1,
                                hintedArrowId = null,
                                blockedBump = null
                            )
                        }
                        levelTransitionJob?.cancel()
                        levelTransitionJob = viewModelScope.launch {
                            delay(480L)
                            completeTutorial(emptyList())
                        }
                    } else {
                        val nextHint = PuzzleSolver.findBestHintArrow(
                            nextRemaining,
                            state.level.rows,
                            state.level.cols
                        )?.id
                        _uiState.update {
                            it.copy(
                                remainingArrows = nextRemaining,
                                allInspections = computeInspections(nextRemaining, state.level.rows, state.level.cols),
                                movesCount = it.movesCount + 1,
                                hintedArrowId = nextHint,
                                tutorialStep = TutorialStep.FINAL_CLEAR_ARROW,
                                blockedBump = null
                            )
                        }
                    }
                } else {
                    // Never lose hearts in tutorial!
                    soundEngine.playBlockedBump()
                    eventCounter++
                    _uiState.update {
                        it.copy(
                            blockedBump = BlockedBumpFeedback(
                                arrowId = arrow.id,
                                blockerCell = inspection.blockerCell,
                                rayCells = inspection.rayCells,
                                eventSeq = eventCounter
                            )
                        )
                    }
                }
            }

            else -> Unit
        }
    }

    private fun removeArrowWithAnimation(arrow: ArrowTile, state: GameplayUiState) {
        enqueueEscapingArrow(arrow)
        val nextRemaining = state.remainingArrows.filterNot { it.id == arrow.id }
        val newInspections = computeInspections(nextRemaining, state.level.rows, state.level.cols)

        if (nextRemaining.isEmpty()) {
            val finalMoves = state.movesCount + 1
            _uiState.update {
                it.copy(
                    remainingArrows = emptyList(),
                    allInspections = emptyMap(),
                    movesCount = finalMoves,
                    hintedArrowId = null,
                    blockedBump = null
                )
            }
            levelTransitionJob?.cancel()
            levelTransitionJob = viewModelScope.launch {
                delay(480L)
                handleLevelWon(state.copy(remainingArrows = emptyList(), movesCount = finalMoves))
            }
        } else {
            _uiState.update {
                it.copy(
                    remainingArrows = nextRemaining,
                    allInspections = newInspections,
                    movesCount = it.movesCount + 1,
                    hintedArrowId = if (it.hintedArrowId == arrow.id) null else it.hintedArrowId,
                    blockedBump = null
                )
            }
        }
    }

    private fun enqueueEscapingArrow(arrow: ArrowTile) {
        eventCounter++
        val seq = eventCounter
        _uiState.update {
            it.copy(escapingArrows = it.escapingArrows + EscapingArrow(arrow, seq))
        }
        viewModelScope.launch {
            delay(620L)
            _uiState.update { st ->
                st.copy(escapingArrows = st.escapingArrows.filterNot { it.eventSeq == seq })
            }
        }
    }

    private fun completeTutorial(remaining: List<ArrowTile>) {
        timerJob?.cancel()
        soundEngine.playLevelComplete()
        viewModelScope.launch {
            repository.setTutorialCompleted(true)
        }
        _uiState.update {
            it.copy(
                remainingArrows = remaining,
                hintedArrowId = null,
                tutorialStep = TutorialStep.COMPLETED,
                status = GameplayStatus.LEVEL_COMPLETED,
                earnedStars = 3
            )
        }
    }

    fun skipTutorial(onSkipped: () -> Unit) {
        soundEngine.playButtonClick()
        timerJob?.cancel()
        viewModelScope.launch {
            repository.setTutorialCompleted(true)
        }
        onSkipped()
    }

    private fun handleLevelWon(wonState: GameplayUiState) {
        timerJob?.cancel()
        soundEngine.playLevelComplete()
        val stars = wonState.hearts.coerceIn(1, 3)
        val level = wonState.level

        _uiState.update {
            it.copy(
                remainingArrows = emptyList(),
                movesCount = wonState.movesCount,
                hintedArrowId = null,
                blockedBump = null,
                status = GameplayStatus.LEVEL_COMPLETED,
                earnedStars = stars
            )
        }

        viewModelScope.launch {
            when (level.mode) {
                GameMode.CAMPAIGN -> {
                    val bonusHints = repository.recordCampaignWin(
                        levelNumber = level.levelNumber,
                        stars = stars,
                        timeSeconds = wonState.elapsedSeconds
                    )
                    if (bonusHints > 0) soundEngine.playReward()
                    _uiState.update { it.copy(earnedBonusHints = bonusHints) }
                }

                GameMode.DAILY_CHALLENGE -> {
                    val epochDay = level.dailyEpochDay ?: DateHelper.currentEpochDay()
                    val bonusHints = repository.recordDailyWin(
                        epochDay = epochDay,
                        dayOfMonth = level.levelNumber,
                        yearMonth = DateHelper.currentYearMonthKey(),
                        stars = stars
                    )
                    if (bonusHints > 0) soundEngine.playReward()
                    _uiState.update { it.copy(earnedBonusHints = bonusHints) }
                }

                GameMode.TOURNAMENT -> {
                    val stage = level.tournamentStage ?: 1
                    val speedBonus = (120 - wonState.elapsedSeconds).coerceAtLeast(0) * 4
                    val stageScore = 450 + stage * 150 + stars * 120 + speedBonus
                    val (entry, bonusHints) = repository.recordTournamentStageWin(
                        eventId = DateHelper.currentTournamentEventId(),
                        stageCompleted = stage,
                        stageScore = stageScore
                    )
                    if (bonusHints > 0 || entry.trophiesEarned > 0) soundEngine.playReward()
                    _uiState.update {
                        it.copy(
                            earnedTournamentScore = stageScore,
                            tournamentTrophies = entry.trophiesEarned,
                            earnedBonusHints = bonusHints
                        )
                    }
                }

                GameMode.TUTORIAL -> Unit
            }
        }
    }

    fun useHint(): Boolean {
        val state = _uiState.value
        if (state.status != GameplayStatus.PLAYING || state.remainingArrows.isEmpty()) return false

        val best = PuzzleSolver.findBestHintArrow(
            remainingArrows = state.remainingArrows,
            rows = state.level.rows,
            cols = state.level.cols
        ) ?: return false

        if (state.level.mode == GameMode.TUTORIAL) {
            soundEngine.playReward()
            _uiState.update { it.copy(hintedArrowId = best.id) }
            return true
        }

        if (state.hintedArrowId == best.id) {
            soundEngine.playButtonClick()
            return true
        }

        viewModelScope.launch {
            val spent = repository.spendHint()
            if (spent) {
                soundEngine.playReward()
                _uiState.update { it.copy(hintedArrowId = best.id) }
            } else {
                soundEngine.playBlockedBump()
            }
        }
        return true
    }

    fun toggleRulerTool() {
        soundEngine.playButtonClick()
        _uiState.update { state ->
            val next = !state.isRulerActive
            state.copy(
                isRulerActive = next,
                allInspections = computeInspections(
                    state.remainingArrows,
                    state.level.rows,
                    state.level.cols
                )
            )
        }
    }

    private fun computeInspections(
        arrows: List<ArrowTile>,
        rows: Int,
        cols: Int
    ): Map<Int, PathInspection> {
        return arrows.associate { arrow ->
            arrow.id to PuzzleSolver.inspectArrowPath(arrow, arrows, rows, cols)
        }
    }

    private fun GameplayUiState.rowsOrDefault(): Int = level.rows.coerceAtLeast(1)
    private fun GameplayUiState.colsOrDefault(): Int = level.cols.coerceAtLeast(1)

    override fun onCleared() {
        timerJob?.cancel()
        levelTransitionJob?.cancel()
        super.onCleared()
    }
}
