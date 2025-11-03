package com.example.playlistmaker.sharing.domain.interactor

import com.example.playlistmaker.sharing.domain.repository.SharingRepository

class SharingInteractorImpl(
    private val repository: SharingRepository
) : SharingInteractor {

    override fun getShareAppContent(): String = repository.getShareAppContent()

    override fun getSupportEmailData(): Triple<String, String, String> =
        repository.getSupportEmailData()

    override fun getTermsUrl(): String = repository.getTermsUrl()
}