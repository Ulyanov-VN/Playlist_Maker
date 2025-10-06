package com.example.playlistmaker.data.repository

import android.content.SharedPreferences
import com.example.playlistmaker.domain.repository.SettingsRepository

class SettingsRepositoryImpl(
    private val prefs: SharedPreferences
) : SettingsRepository {

    companion object {
        private const val KEY_THEME = "is_night_mode"
    }

    override fun getTheme(): Boolean {
        return prefs.getBoolean(KEY_THEME, false)
    }

    override fun setTheme(isNightMode: Boolean) {
        prefs.edit().putBoolean(KEY_THEME, isNightMode).apply()
    }
}