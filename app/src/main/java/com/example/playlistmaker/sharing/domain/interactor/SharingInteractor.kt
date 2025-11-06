package com.example.playlistmaker.sharing.domain.interactor

interface SharingInteractor {
    fun getShareAppContent(): String
    fun getSupportEmailData(): Triple<String, String, String>
    fun getTermsUrl(): String
}
