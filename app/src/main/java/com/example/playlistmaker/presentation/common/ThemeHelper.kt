package com.example.playlistmaker.presentation.common

import android.app.Activity
import android.content.Context
import androidx.appcompat.app.AppCompatDelegate

object ThemeHelper {

    private const val PREFS_NAME = "theme_prefs"
    private const val KEY_THEME = "is_night_mode"

    fun applyTheme(activity: Activity) {
        val prefs = activity.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val isNightMode = prefs.getBoolean(KEY_THEME, false)
        setNightMode(isNightMode)
    }

    fun toggleTheme(activity: Activity, isNightMode: Boolean) {
        setNightMode(isNightMode)
        saveTheme(activity, isNightMode)
    }

    private fun setNightMode(isNightMode: Boolean) {
        AppCompatDelegate.setDefaultNightMode(
            if (isNightMode) AppCompatDelegate.MODE_NIGHT_YES
            else AppCompatDelegate.MODE_NIGHT_NO
        )
    }

    private fun saveTheme(activity: Activity, isNightMode: Boolean) {
        activity.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_THEME, isNightMode)
            .apply()
    }
}