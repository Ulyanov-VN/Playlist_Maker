package com.example.playlistmaker.domain.interactor

import com.example.playlistmaker.domain.repository.SettingsRepository

interface ManageThemeInteractor {
    fun isDarkThemeEnabled(): Boolean
    fun setDarkThemeEnabled(isEnabled: Boolean)
}

class ManageThemeInteractorImpl(
    private val settingsRepository: SettingsRepository
) : ManageThemeInteractor {
    override fun isDarkThemeEnabled(): Boolean = settingsRepository.getTheme()
    override fun setDarkThemeEnabled(isEnabled: Boolean) = settingsRepository.setTheme(isEnabled)
}