package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import java.lang.ref.WeakReference

@Database(
    entities = [
        LevelProgressEntity::class,
        DailyChallengeEntity::class,
        TournamentEntryEntity::class,
        UnlockedAchievementEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun levelProgressDao(): LevelProgressDao
    abstract fun dailyChallengeDao(): DailyChallengeDao
    abstract fun tournamentDao(): TournamentDao
    abstract fun achievementDao(): AchievementDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        @Volatile
        private var boundAppContext: WeakReference<Context>? = null

        fun getInstance(context: Context): AppDatabase {
            val appCtx = context.applicationContext
            val existing = INSTANCE
            if (existing != null && boundAppContext?.get() === appCtx) {
                return existing
            }
            return synchronized(this) {
                val recheck = INSTANCE
                if (recheck != null && boundAppContext?.get() === appCtx) {
                    recheck
                } else {
                    recheck?.close()
                    Room.databaseBuilder(
                        appCtx,
                        AppDatabase::class.java,
                        "arrow_puzzle_db"
                    ).build().also {
                        INSTANCE = it
                        boundAppContext = WeakReference(appCtx)
                    }
                }
            }
        }
    }
}
