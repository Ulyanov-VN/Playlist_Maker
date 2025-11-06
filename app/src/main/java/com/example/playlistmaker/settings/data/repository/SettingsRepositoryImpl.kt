package com.example.playlistmaker.settings.data.repository

import android.content.SharedPreferences
import com.example.playlistmaker.settings.domain.repository.SettingsRepository

class SettingsRepositoryImpl(
    private val prefs: SharedPreferences
) : SettingsRepository {

    override fun getTheme(): Boolean {
        return prefs.getBoolean(KEY_THEME, false)
    }

    override fun setTheme(isNightMode: Boolean) {
        prefs.edit().putBoolean(KEY_THEME, isNightMode).apply()
    }

    companion object {
        private const val KEY_THEME = "is_night_mode"
    }
}