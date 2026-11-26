package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface LevelProgressDao {
    @Query("SELECT * FROM level_progress ORDER BY levelNumber ASC")
    fun observeAll(): Flow<List<LevelProgressEntity>>

    @Query("SELECT SUM(stars) FROM level_progress")
    fun observeTotalStars(): Flow<Int?>

    @Query("SELECT COUNT(*) FROM level_progress")
    fun observeCompletedCount(): Flow<Int>

    @Query("SELECT * FROM level_progress WHERE levelNumber = :level")
    suspend fun getLevel(level: Int): LevelProgressEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: LevelProgressEntity)

    @Query("DELETE FROM level_progress")
    suspend fun deleteAll()
}

@Dao
interface DailyChallengeDao {
    @Query("SELECT * FROM daily_challenges WHERE yearMonth = :yearMonth ORDER BY dayOfMonth ASC")
    fun observeForMonth(yearMonth: String): Flow<List<DailyChallengeEntity>>

    @Query("SELECT * FROM daily_challenges ORDER BY epochDay DESC")
    fun observeAll(): Flow<List<DailyChallengeEntity>>

    @Query("SELECT SUM(stars) FROM daily_challenges")
    fun observeTotalDailyStars(): Flow<Int?>

    @Query("SELECT * FROM daily_challenges WHERE epochDay = :epochDay")
    suspend fun getDay(epochDay: Long): DailyChallengeEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: DailyChallengeEntity)

    @Query("DELETE FROM daily_challenges")
    suspend fun deleteAll()
}

@Dao
interface TournamentDao {
    @Query("SELECT * FROM tournament_entries WHERE eventId = :eventId")
    fun observeEvent(eventId: Int): Flow<TournamentEntryEntity?>

    @Query("SELECT SUM(trophiesEarned) FROM tournament_entries")
    fun observeTotalTrophies(): Flow<Int?>

    @Query("SELECT * FROM tournament_entries WHERE eventId = :eventId")
    suspend fun getEvent(eventId: Int): TournamentEntryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: TournamentEntryEntity)

    @Query("DELETE FROM tournament_entries")
    suspend fun deleteAll()
}

@Dao
interface AchievementDao {
    @Query("SELECT * FROM unlocked_achievements ORDER BY unlockedAtMillis DESC")
    fun observeUnlocked(): Flow<List<UnlockedAchievementEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(entity: UnlockedAchievementEntity)

    @Query("DELETE FROM unlocked_achievements")
    suspend fun deleteAll()
}
