package com.example.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStoreFile
import com.example.domain.AppLanguage
import com.example.domain.GraphicsQuality
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.lang.ref.WeakReference

private object DataStoreHolder {
    @Volatile
    private var instance: DataStore<Preferences>? = null
    @Volatile
    private var boundCtx: WeakReference<Context>? = null

    fun get(context: Context): DataStore<Preferences> {
        val appCtx = context.applicationContext
        val existing = instance
        if (existing != null && boundCtx?.get() === appCtx) return existing
        return synchronized(this) {
            val recheck = instance
            if (recheck != null && boundCtx?.get() === appCtx) {
                recheck
            } else {
                PreferenceDataStoreFactory.create(
                    scope = CoroutineScope(Dispatchers.IO + SupervisorJob()),
                    produceFile = { appCtx.preferencesDataStoreFile("arrow_puzzle_prefs") }
                ).also {
                    instance = it
                    boundCtx = WeakReference(appCtx)
                }
            }
        }
    }
}

data class PlayerSettingsState(
    val unlockedLevel: Int = 1,
    val lastPlayedLevel: Int = 1,
    val tutorialCompleted: Boolean = false,
    val hintBalance: Int = 5,
    val musicEnabled: Boolean = true,
    val soundEffectsEnabled: Boolean = true,
    val vibrationEnabled: Boolean = true,
    val language: AppLanguage = AppLanguage.ENGLISH,
    val graphicsQuality: GraphicsQuality = GraphicsQuality.HIGH,
    val playerName: String = "Arrow Ace",
    val analyticsConsent: Boolean = false,
    val personalizedTipsConsent: Boolean = true
)

class UserPreferences(context: Context) {

    private val ds = DataStoreHolder.get(context)

    private object Keys {
        val UNLOCKED_LEVEL = intPreferencesKey("unlocked_level")
        val LAST_PLAYED_LEVEL = intPreferencesKey("last_played_level")
        val TUTORIAL_COMPLETED = booleanPreferencesKey("tutorial_completed")
        val HINT_BALANCE = intPreferencesKey("hint_balance")
        val MUSIC_ENABLED = booleanPreferencesKey("music_enabled")
        val SFX_ENABLED = booleanPreferencesKey("sfx_enabled")
        val VIBRATION_ENABLED = booleanPreferencesKey("vibration_enabled")
        val LANGUAGE_CODE = stringPreferencesKey("language_code")
        val GRAPHICS_QUALITY = stringPreferencesKey("graphics_quality")
        val PLAYER_NAME = stringPreferencesKey("player_name")
        val ANALYTICS_CONSENT = booleanPreferencesKey("analytics_consent")
        val PERSONALIZED_TIPS = booleanPreferencesKey("personalized_tips")
    }

    val stateFlow: Flow<PlayerSettingsState> = ds.data.map { prefs ->
        PlayerSettingsState(
            unlockedLevel = prefs[Keys.UNLOCKED_LEVEL] ?: 1,
            lastPlayedLevel = prefs[Keys.LAST_PLAYED_LEVEL] ?: 1,
            tutorialCompleted = prefs[Keys.TUTORIAL_COMPLETED] ?: false,
            hintBalance = prefs[Keys.HINT_BALANCE] ?: 5,
            musicEnabled = prefs[Keys.MUSIC_ENABLED] ?: true,
            soundEffectsEnabled = prefs[Keys.SFX_ENABLED] ?: true,
            vibrationEnabled = prefs[Keys.VIBRATION_ENABLED] ?: true,
            language = AppLanguage.fromCode(prefs[Keys.LANGUAGE_CODE] ?: "en"),
            graphicsQuality = GraphicsQuality.fromName(prefs[Keys.GRAPHICS_QUALITY] ?: GraphicsQuality.HIGH.name),
            playerName = prefs[Keys.PLAYER_NAME] ?: "Arrow Ace",
            analyticsConsent = prefs[Keys.ANALYTICS_CONSENT] ?: false,
            personalizedTipsConsent = prefs[Keys.PERSONALIZED_TIPS] ?: true
        )
    }

    suspend fun setTutorialCompleted(completed: Boolean) {
        ds.edit { it[Keys.TUTORIAL_COMPLETED] = completed }
    }

    suspend fun setLastPlayedLevel(level: Int) {
        ds.edit { it[Keys.LAST_PLAYED_LEVEL] = level.coerceAtLeast(1) }
    }

    suspend fun unlockNextLevelIfNeeded(completedLevel: Int) {
        ds.edit { prefs ->
            val current = prefs[Keys.UNLOCKED_LEVEL] ?: 1
            if (completedLevel >= current) {
                prefs[Keys.UNLOCKED_LEVEL] = completedLevel + 1
            }
            prefs[Keys.LAST_PLAYED_LEVEL] = completedLevel + 1
        }
    }

    suspend fun spendHint(): Boolean {
        var spent = false
        ds.edit { prefs ->
            val current = prefs[Keys.HINT_BALANCE] ?: 5
            if (current > 0) {
                prefs[Keys.HINT_BALANCE] = current - 1
                spent = true
            }
        }
        return spent
    }

    suspend fun addHints(amount: Int) {
        if (amount <= 0) return
        ds.edit { prefs ->
            val current = prefs[Keys.HINT_BALANCE] ?: 5
            prefs[Keys.HINT_BALANCE] = current + amount
        }
    }

    suspend fun setMusicEnabled(enabled: Boolean) {
        ds.edit { it[Keys.MUSIC_ENABLED] = enabled }
    }

    suspend fun setSoundEffectsEnabled(enabled: Boolean) {
        ds.edit { it[Keys.SFX_ENABLED] = enabled }
    }

    suspend fun setVibrationEnabled(enabled: Boolean) {
        ds.edit { it[Keys.VIBRATION_ENABLED] = enabled }
    }

    suspend fun setLanguage(language: AppLanguage) {
        ds.edit { it[Keys.LANGUAGE_CODE] = language.code }
    }

    suspend fun setGraphicsQuality(quality: GraphicsQuality) {
        ds.edit { it[Keys.GRAPHICS_QUALITY] = quality.name }
    }

    suspend fun setPlayerName(name: String) {
        val cleaned = name.trim().take(20).ifEmpty { "Arrow Ace" }
        ds.edit { it[Keys.PLAYER_NAME] = cleaned }
    }

    suspend fun setPrivacyPreferences(analytics: Boolean, tips: Boolean) {
        ds.edit {
            it[Keys.ANALYTICS_CONSENT] = analytics
            it[Keys.PERSONALIZED_TIPS] = tips
        }
    }

    suspend fun resetProgress() {
        ds.edit { prefs ->
            prefs[Keys.UNLOCKED_LEVEL] = 1
            prefs[Keys.LAST_PLAYED_LEVEL] = 1
            prefs[Keys.HINT_BALANCE] = 5
            // Keep tutorialCompleted=false? Actually resetting progress lets them replay or stay clean;
            // keep settings (music, sfx, language) intact while resetting game progress!
        }
    }
}
