package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.SoundEngine
import com.example.data.AppDatabase
import com.example.data.DailyChallengeEntity
import com.example.data.GameRepository
import com.example.data.PlayerStatsSummary
import com.example.data.TournamentEntryEntity
import com.example.data.UserPreferences
import com.example.domain.AppLanguage
import com.example.domain.DateHelper
import com.example.domain.GameStrings
import com.example.domain.GraphicsQuality
import com.example.domain.LocalizedStrings
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AppViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getInstance(application)
    private val userPreferences = UserPreferences(application)
    val repository = GameRepository(database, userPreferences)
    val soundEngine = SoundEngine(application)

    val isReady: StateFlow<Boolean> = repository.playerSummary
        .map { true }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = false
        )

    val playerSummary: StateFlow<PlayerStatsSummary> = repository.playerSummary
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = PlayerStatsSummary()
        )

    val localizedStrings: StateFlow<LocalizedStrings> = playerSummary
        .map { GameStrings.forLanguage(it.settings.language) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = GameStrings.forLanguage(AppLanguage.ENGLISH)
        )

    val currentMonthDailyMap: StateFlow<Map<Int, DailyChallengeEntity>> =
        repository.observeDailyMonth(DateHelper.currentYearMonthKey())
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyMap()
            )

    val currentTournamentEntry: StateFlow<TournamentEntryEntity?> =
        repository.observeTournamentEntry(DateHelper.currentTournamentEventId())
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = null
            )

    init {
        viewModelScope.launch {
            playerSummary.collect { summary ->
                soundEngine.updateSettings(
                    sfx = summary.settings.soundEffectsEnabled,
                    music = summary.settings.musicEnabled,
                    vibration = summary.settings.vibrationEnabled
                )
            }
        }
    }

    fun setMusicEnabled(enabled: Boolean) = viewModelScope.launch {
        repository.setMusicEnabled(enabled)
    }

    fun setSoundEffectsEnabled(enabled: Boolean) = viewModelScope.launch {
        repository.setSoundEffectsEnabled(enabled)
    }

    fun setVibrationEnabled(enabled: Boolean) = viewModelScope.launch {
        repository.setVibrationEnabled(enabled)
    }

    fun setLanguage(language: AppLanguage) = viewModelScope.launch {
        repository.setLanguage(language)
    }

    fun setGraphicsQuality(quality: GraphicsQuality) = viewModelScope.launch {
        repository.setGraphicsQuality(quality)
    }

    fun setPlayerName(name: String) = viewModelScope.launch {
        repository.setPlayerName(name)
    }

    fun setPrivacyPreferences(analytics: Boolean, tips: Boolean) = viewModelScope.launch {
        repository.setPrivacyPreferences(analytics, tips)
    }

    fun resetProgress() = viewModelScope.launch {
        repository.resetAllProgress()
    }

    fun markTutorialCompleted() = viewModelScope.launch {
        repository.setTutorialCompleted(true)
    }
}
