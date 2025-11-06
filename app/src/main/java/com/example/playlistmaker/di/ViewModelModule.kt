package com.example.playlistmaker.di

import com.example.playlistmaker.player.ui.viewmodels.PlayerViewModel
import com.example.playlistmaker.search.ui.viewmodels.SearchViewModel
import com.example.playlistmaker.settings.ui.viewmodels.SettingsViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val viewModelModule = module {

    viewModel {
        SearchViewModel(
            searchTracksInteractor = get(),
            manageSearchHistoryInteractor = get(),
            // добавили форматтер, чтобы Activity не тянула зависимости:
            formatTimeInteractor = get()
        )
    }

    viewModel {
        PlayerViewModel(
            playerInteractor = get(),
            formatTimeInteractor = get(),
            getCountryNameInteractor = get(),
            getCoverArtworkInteractor = get(),
            getReleaseYearInteractor = get()
        )
    }

    viewModel {
        SettingsViewModel(
            manageThemeInteractor = get(),
            sharingInteractor = get()
        )
    }
}
