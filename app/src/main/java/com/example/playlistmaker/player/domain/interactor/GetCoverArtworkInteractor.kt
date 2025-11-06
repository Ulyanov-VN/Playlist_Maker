package com.example.playlistmaker.player.domain.interactor

interface GetCoverArtworkInteractor {
    fun execute(artworkUrl100: String?): String?
}

class GetCoverArtworkInteractorImpl : GetCoverArtworkInteractor {
    override fun execute(artworkUrl100: String?): String? {
        return artworkUrl100?.replace("100x100", "512x512")
    }
}