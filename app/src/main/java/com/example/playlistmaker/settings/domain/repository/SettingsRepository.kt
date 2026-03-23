package com.example.playlistmaker.settings.domain.repository

interface SettingsRepository {
    fun getTheme(): Boolean
    fun setTheme(isNightMode: Boolean)
}