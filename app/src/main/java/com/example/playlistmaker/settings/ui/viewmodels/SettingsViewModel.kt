package com.example.playlistmaker.settings.ui.viewmodels

import androidx.lifecycle.ViewModel
import com.example.playlistmaker.settings.domain.interactor.ManageThemeInteractor
import com.example.playlistmaker.sharing.domain.interactor.SharingInteractor

class SettingsViewModel(
    private val manageThemeInteractor: ManageThemeInteractor,
    private val sharingInteractor: SharingInteractor
) : ViewModel() {

    fun isDarkThemeEnabled(): Boolean = manageThemeInteractor.isDarkThemeEnabled()

    fun setDarkThemeEnabled(isEnabled: Boolean) {
        manageThemeInteractor.setDarkThemeEnabled(isEnabled)
    }

    fun getShareAppContent(): String = sharingInteractor.getShareAppContent()
    fun getSupportEmailData(): Triple<String, String, String> = sharingInteractor.getSupportEmailData()
    fun getTermsUrl(): String = sharingInteractor.getTermsUrl()
}