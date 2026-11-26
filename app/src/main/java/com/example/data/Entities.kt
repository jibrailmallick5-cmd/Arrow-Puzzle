package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "level_progress")
data class LevelProgressEntity(
    @PrimaryKey val levelNumber: Int,
    val stars: Int,
    val bestTimeSeconds: Int,
    val completedAtMillis: Long
)

@Entity(tableName = "daily_challenges")
data class DailyChallengeEntity(
    @PrimaryKey val epochDay: Long,
    val yearMonth: String,
    val dayOfMonth: Int,
    val stars: Int,
    val completedAtMillis: Long
)

@Entity(tableName = "tournament_entries")
data class TournamentEntryEntity(
    @PrimaryKey val eventId: Int,
    val stageReached: Int,
    val bestScore: Int,
    val trophiesEarned: Int,
    val completedEvent: Boolean,
    val updatedAtMillis: Long
)

@Entity(tableName = "unlocked_achievements")
data class UnlockedAchievementEntity(
    @PrimaryKey val achievementId: String,
    val unlockedAtMillis: Long
)
