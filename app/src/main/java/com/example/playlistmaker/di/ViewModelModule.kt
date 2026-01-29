package com.example.playlistmaker.di

import com.example.playlistmaker.favorites.domain.interactor.FavoriteTracksInteractor
import com.example.playlistmaker.media.ui.viewmodels.FavoriteTracksViewModel
import com.example.playlistmaker.media.ui.viewmodels.PlaylistsViewModel
import com.example.playlistmaker.search.ui.viewmodels.SearchViewModel
import com.example.playlistmaker.settings.ui.viewmodels.SettingsViewModel
import com.example.playlistmaker.player.ui.viewmodels.PlayerViewModel
import com.example.playlistmaker.playlist.domain.interactor.PlaylistInteractor
import com.example.playlistmaker.playlist.ui.viewmodels.CreatePlaylistViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val viewModelModule = module {

    viewModel {
        SearchViewModel(
            searchTracksInteractor = get(),
            manageSearchHistoryInteractor = get(),
            formatTimeInteractor = get()
        )
    }

    viewModel {
        PlayerViewModel(
            playerInteractor = get(),
            formatTimeInteractor = get(),
            getCountryNameInteractor = get(),
            getCoverArtworkInteractor = get(),
            getReleaseYearInteractor = get(),
            favoriteTracksInteractor = get(),
            playlistInteractor = get<PlaylistInteractor>()
        )
    }

    viewModel {
        SettingsViewModel(
            manageThemeInteractor = get(),
            sharingInteractor = get()
        )
    }

    viewModel {
        PlaylistsViewModel(
            playlistInteractor = get()
        )
    }

    viewModel {
        FavoriteTracksViewModel(
            favoriteTracksInteractor = get()
        )
    }
    viewModel {
        CreatePlaylistViewModel(
            playlistInteractor = get()
        )
    }
}