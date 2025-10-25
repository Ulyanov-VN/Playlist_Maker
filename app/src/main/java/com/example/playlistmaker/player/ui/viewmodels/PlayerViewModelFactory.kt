package com.example.playlistmaker.player.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.playlistmaker.player.domain.interactor.*

class PlayerViewModelFactory(
    private val playerInteractor: PlayerInteractor,
    private val formatTimeInteractor: FormatTimeInteractor,
    private val getCountryNameInteractor: GetCountryNameInteractor,
    private val getCoverArtworkInteractor: GetCoverArtworkInteractor,
    private val getReleaseYearInteractor: GetReleaseYearInteractor
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return PlayerViewModel(
            playerInteractor,
            formatTimeInteractor,
            getCountryNameInteractor,
            getCoverArtworkInteractor,
            getReleaseYearInteractor
        ) as T
    }
}