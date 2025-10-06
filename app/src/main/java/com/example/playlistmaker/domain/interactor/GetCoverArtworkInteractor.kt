package com.example.playlistmaker.domain.interactor

class GetCoverArtworkInteractor {
    fun execute(artworkUrl100: String?): String? {
        return artworkUrl100?.replace("100x100", "512x512")
    }
}