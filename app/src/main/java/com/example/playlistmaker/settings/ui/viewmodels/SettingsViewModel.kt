package com.example.playlistmaker.settings.ui.viewmodels

import androidx.lifecycle.ViewModel
import com.example.playlistmaker.settings.domain.interactor.ManageThemeInteractor

class SettingsViewModel(
    private val manageThemeInteractor: ManageThemeInteractor
) : ViewModel() {

    fun isDarkThemeEnabled(): Boolean = manageThemeInteractor.isDarkThemeEnabled()

    fun setDarkThemeEnabled(isEnabled: Boolean) {
        manageThemeInteractor.setDarkThemeEnabled(isEnabled)
    }
}