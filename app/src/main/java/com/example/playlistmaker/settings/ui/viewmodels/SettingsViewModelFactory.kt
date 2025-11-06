package com.example.playlistmaker.settings.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.playlistmaker.settings.domain.interactor.ManageThemeInteractor
import com.example.playlistmaker.sharing.domain.interactor.SharingInteractor

class SettingsViewModelFactory(
    private val manageThemeInteractor: ManageThemeInteractor,
    private val sharingInteractor: SharingInteractor
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return SettingsViewModel(manageThemeInteractor, sharingInteractor) as T
    }
}