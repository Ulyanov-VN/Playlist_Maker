package com.example.playlistmaker.domain.interactor

class GetReleaseYearInteractor {
    fun execute(releaseDate: String?): String? {
        return releaseDate?.take(4)
    }
}