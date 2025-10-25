package com.example.playlistmaker.sharing.domain.interactor

interface SharingInteractor {
    fun getShareAppContent(): String
    fun getSupportEmailData(): Triple<String, String, String>
    fun getTermsUrl(): String
}

class SharingInteractorImpl : SharingInteractor {
    override fun getShareAppContent(): String = "Check out this cool music app - Playlist Maker!"

    override fun getSupportEmailData(): Triple<String, String, String> =
        Triple(
            "support@playlistmaker.com",
            "Support Request - Playlist Maker",
            "Hello, I need help with Playlist Maker app..."
        )

    override fun getTermsUrl(): String = "https://playlistmaker.example.com/terms-and-conditions"
}