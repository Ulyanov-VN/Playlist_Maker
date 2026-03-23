package com.example.playlistmaker.player.domain.interactor

interface GetReleaseYearInteractor {
    fun execute(releaseDate: String?): String?
}

class GetReleaseYearInteractorImpl : GetReleaseYearInteractor {
    override fun execute(releaseDate: String?): String? {
        return releaseDate?.take(4)
    }
}