package com.example.playlistmaker.settings.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.playlistmaker.settings.domain.interactor.ManageThemeInteractor

class SettingsViewModelFactory(
    private val manageThemeInteractor: ManageThemeInteractor
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return SettingsViewModel(manageThemeInteractor) as T
    }
}