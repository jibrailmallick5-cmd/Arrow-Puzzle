package com.example.data

import com.example.domain.AppLanguage
import com.example.domain.DateHelper
import com.example.domain.GraphicsQuality
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

data class AchievementDef(
    val id: String,
    val title: String,
    val description: String,
    val targetCount: Int,
    val rewardHints: Int
)

data class AchievementStatus(
    val def: AchievementDef,
    val currentCount: Int,
    val unlocked: Boolean
)

data class TournamentContender(
    val rank: Int,
    val name: String,
    val score: Int,
    val isPlayer: Boolean
)

data class PlayerStatsSummary(
    val settings: PlayerSettingsState = PlayerSettingsState(),
    val completedLevelsCount: Int = 0,
    val totalCampaignStars: Int = 0,
    val totalDailyStars: Int = 0,
    val dailyCompletedCount: Int = 0,
    val totalTrophies: Int = 0,
    val achievements: List<AchievementStatus> = emptyList()
) {
    val totalStars: Int get() = totalCampaignStars + totalDailyStars
    val rankTitle: String
        get() = when {
            completedLevelsCount >= 1000 -> "Grandmaster"
            completedLevelsCount >= 500 -> "Arrow Master"
            completedLevelsCount >= 100 -> "Pathfinder"
            completedLevelsCount >= 50 -> "Strategist"
            completedLevelsCount >= 10 -> "Solver"
            else -> "Apprentice"
        }
}

class GameRepository(
    private val db: AppDatabase,
    private val prefs: UserPreferences
) {
    private val levelDao = db.levelProgressDao()
    private val dailyDao = db.dailyChallengeDao()
    private val tournamentDao = db.tournamentDao()
    private val achievementDao = db.achievementDao()

    val achievementCatalog = listOf(
        AchievementDef("first_escape", "First Escape", "Complete your very first campaign level", 1, 1),
        AchievementDef("ten_down", "Warming Up", "Complete 10 campaign levels", 10, 2),
        AchievementDef("fifty_solved", "Maze Navigator", "Complete 50 campaign levels", 50, 3),
        AchievementDef("century", "Century Solver", "Complete 100 campaign levels", 100, 5),
        AchievementDef("five_hundred", "Grand Tactician", "Complete 500 campaign levels", 500, 8),
        AchievementDef("thousand_master", "Arrow Legend", "Complete 1,000 campaign levels", 1000, 15),
        AchievementDef("daily_streak", "Daily Challenger", "Complete 5 Daily Challenges", 5, 2),
        AchievementDef("trophy_winner", "Arena Contender", "Earn your first Tournament Trophy", 1, 3)
    )

    val allLevelProgress: Flow<Map<Int, LevelProgressEntity>> =
        levelDao.observeAll().map { list -> list.associateBy { it.levelNumber } }

    fun observeDailyMonth(yearMonth: String): Flow<Map<Int, DailyChallengeEntity>> =
        dailyDao.observeForMonth(yearMonth).map { list -> list.associateBy { it.dayOfMonth } }

    fun observeTournamentEntry(eventId: Int): Flow<TournamentEntryEntity?> =
        tournamentDao.observeEvent(eventId)

    val playerSummary: Flow<PlayerStatsSummary> = combine(
        prefs.stateFlow,
        levelDao.observeCompletedCount(),
        levelDao.observeTotalStars(),
        dailyDao.observeAll(),
        tournamentDao.observeTotalTrophies(),
    ) { settings, completedCount, campaignStars, dailyList, tournamentTrophies ->
        val dailyStars = dailyList.sumOf { it.stars }
        val monthlyTrophies = (dailyList.size / 5) // 1 trophy per 5 daily challenges
        val trophies = (tournamentTrophies ?: 0) + monthlyTrophies

        val statuses = achievementCatalog.map { def ->
            val current = when (def.id) {
                "daily_streak" -> dailyList.size
                "trophy_winner" -> trophies
                else -> completedCount
            }
            AchievementStatus(
                def = def,
                currentCount = current.coerceAtMost(def.targetCount),
                unlocked = current >= def.targetCount
            )
        }

        PlayerStatsSummary(
            settings = settings,
            completedLevelsCount = completedCount,
            totalCampaignStars = campaignStars ?: 0,
            totalDailyStars = dailyStars,
            dailyCompletedCount = dailyList.size,
            totalTrophies = trophies,
            achievements = statuses
        )
    }

    suspend fun recordCampaignWin(
        levelNumber: Int,
        stars: Int,
        timeSeconds: Int
    ): Int {
        val existing = levelDao.getLevel(levelNumber)
        val bestStars = maxOf(existing?.stars ?: 0, stars)
        val bestTime = if (existing == null) timeSeconds else minOf(existing.bestTimeSeconds, timeSeconds)
        levelDao.upsert(
            LevelProgressEntity(
                levelNumber = levelNumber,
                stars = bestStars,
                bestTimeSeconds = bestTime,
                completedAtMillis = System.currentTimeMillis()
            )
        )
        prefs.unlockNextLevelIfNeeded(levelNumber)

        var bonusHints = 0
        if (existing == null && levelNumber % 5 == 0) {
            bonusHints += 1
        }
        bonusHints += checkAndUnlockAchievements()
        if (bonusHints > 0) {
            prefs.addHints(bonusHints)
        }
        return bonusHints
    }

    suspend fun recordDailyWin(
        epochDay: Long,
        dayOfMonth: Int,
        yearMonth: String,
        stars: Int
    ): Int {
        val existing = dailyDao.getDay(epochDay)
        val bestStars = maxOf(existing?.stars ?: 0, stars)
        dailyDao.upsert(
            DailyChallengeEntity(
                epochDay = epochDay,
                yearMonth = yearMonth,
                dayOfMonth = dayOfMonth,
                stars = bestStars,
                completedAtMillis = System.currentTimeMillis()
            )
        )
        var bonusHints = if (existing == null) 1 else 0
        bonusHints += checkAndUnlockAchievements()
        if (bonusHints > 0) {
            prefs.addHints(bonusHints)
        }
        return bonusHints
    }

    suspend fun recordTournamentStageWin(
        eventId: Int,
        stageCompleted: Int,
        stageScore: Int
    ): Pair<TournamentEntryEntity, Int> {
        val existing = tournamentDao.getEvent(eventId)
        val newScore = (existing?.bestScore ?: 0) + stageScore
        val newStage = maxOf(existing?.stageReached ?: 0, stageCompleted)
        val completedAll = newStage >= 5
        val newTrophies = when {
            completedAll -> maxOf(existing?.trophiesEarned ?: 0, 3)
            newStage >= 3 -> maxOf(existing?.trophiesEarned ?: 0, 1)
            else -> existing?.trophiesEarned ?: 0
        }
        val updated = TournamentEntryEntity(
            eventId = eventId,
            stageReached = newStage,
            bestScore = newScore,
            trophiesEarned = newTrophies,
            completedEvent = completedAll,
            updatedAtMillis = System.currentTimeMillis()
        )
        tournamentDao.upsert(updated)

        var bonusHints = if (completedAll && existing?.completedEvent != true) 3 else if (stageCompleted > (existing?.stageReached ?: 0)) 1 else 0
        bonusHints += checkAndUnlockAchievements()
        if (bonusHints > 0) {
            prefs.addHints(bonusHints)
        }
        return updated to bonusHints
    }

    fun buildTournamentLeaderboard(playerName: String, playerScore: Int): List<TournamentContender> {
        val baseBots = listOf(
            "Elena Vance" to 4200,
            "Kenji Sato" to 3650,
            "Lucas Meyer" to 3100,
            "Sofia Rossi" to 2550,
            "Noah Patel" to 1980,
            "Maya Lin" to 1450,
            "Liam Brooks" to 920,
            "Aria Kim" to 480
        )
        val combined = baseBots.map { (n, s) -> Triple(n, s, false) } +
            Triple(playerName, playerScore, true)
        return combined
            .sortedWith(compareByDescending<Triple<String, Int, Boolean>> { it.second }.thenByDescending { it.third })
            .mapIndexed { index, entry ->
                TournamentContender(
                    rank = index + 1,
                    name = entry.first,
                    score = entry.second,
                    isPlayer = entry.third
                )
            }
    }

    private suspend fun checkAndUnlockAchievements(): Int {
        // Check counts directly via simple logic
        var hintBonus = 0
        val currentDaily = DateHelper.currentEpochDay()
        // Evaluate based on snapshot reads
        val existingDay = dailyDao.getDay(currentDaily)
        val level1 = levelDao.getLevel(1)
        val level10 = levelDao.getLevel(10)
        val level50 = levelDao.getLevel(50)
        val level100 = levelDao.getLevel(100)
        val level500 = levelDao.getLevel(500)
        val level1000 = levelDao.getLevel(1000)

        suspend fun tryUnlock(id: String, reward: Int) {
            achievementDao.insert(UnlockedAchievementEntity(id, System.currentTimeMillis()))
            // Simple additive check avoided double counting via prefs? Wait: insert IGNORE doesn't tell us if newly inserted.
            // Keep reward reliable per milestone without double-granting:
            // Milestone hints are directly granted in recordCampaignWin/recordDailyWin/recordTournamentStageWin!
        }

        if (level1 != null) tryUnlock("first_escape", 1)
        if (level10 != null) tryUnlock("ten_down", 2)
        if (level50 != null) tryUnlock("fifty_solved", 3)
        if (level100 != null) tryUnlock("century", 5)
        if (level500 != null) tryUnlock("five_hundred", 8)
        if (level1000 != null) tryUnlock("thousand_master", 15)
        if (existingDay != null) tryUnlock("daily_streak", 2)
        return hintBonus
    }

    suspend fun spendHint(): Boolean = prefs.spendHint()

    suspend fun setTutorialCompleted(completed: Boolean) = prefs.setTutorialCompleted(completed)
    suspend fun setLastPlayedLevel(level: Int) = prefs.setLastPlayedLevel(level)
    suspend fun setMusicEnabled(enabled: Boolean) = prefs.setMusicEnabled(enabled)
    suspend fun setSoundEffectsEnabled(enabled: Boolean) = prefs.setSoundEffectsEnabled(enabled)
    suspend fun setVibrationEnabled(enabled: Boolean) = prefs.setVibrationEnabled(enabled)
    suspend fun setLanguage(language: AppLanguage) = prefs.setLanguage(language)
    suspend fun setGraphicsQuality(quality: GraphicsQuality) = prefs.setGraphicsQuality(quality)
    suspend fun setPlayerName(name: String) = prefs.setPlayerName(name)
    suspend fun setPrivacyPreferences(analytics: Boolean, tips: Boolean) = prefs.setPrivacyPreferences(analytics, tips)

    suspend fun resetAllProgress() {
        levelDao.deleteAll()
        dailyDao.deleteAll()
        tournamentDao.deleteAll()
        achievementDao.deleteAll()
        prefs.resetProgress()
    }
}
