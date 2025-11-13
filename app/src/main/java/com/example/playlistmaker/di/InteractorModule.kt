package com.example.playlistmaker.di

import com.example.playlistmaker.media.domain.interactor.ManageSearchHistoryInteractor
import com.example.playlistmaker.media.domain.interactor.ManageSearchHistoryInteractorImpl
import com.example.playlistmaker.player.domain.interactor.*
import com.example.playlistmaker.search.domain.interactor.SearchTracksInteractor
import com.example.playlistmaker.search.domain.interactor.SearchTracksInteractorImpl
import com.example.playlistmaker.settings.domain.interactor.ManageThemeInteractor
import com.example.playlistmaker.settings.domain.interactor.ManageThemeInteractorImpl
import com.example.playlistmaker.sharing.domain.interactor.SharingInteractor
import com.example.playlistmaker.sharing.domain.interactor.SharingInteractorImpl
import org.koin.dsl.module

val interactorModule = module {
    // Domain interactors
    single<SearchTracksInteractor> { SearchTracksInteractorImpl(get()) }
    single<ManageSearchHistoryInteractor> { ManageSearchHistoryInteractorImpl(get()) }
    single<ManageThemeInteractor> { ManageThemeInteractorImpl(get()) }
    single<SharingInteractor> { SharingInteractorImpl(get()) }

    // Player utils
    single<FormatTimeInteractor> { FormatTimeInteractorImpl() }
    single<GetCountryNameInteractor> { GetCountryNameInteractorImpl() }
    single<GetCoverArtworkInteractor> { GetCoverArtworkInteractorImpl() }
    single<GetReleaseYearInteractor> { GetReleaseYearInteractorImpl() }

    // Player
    single<PlayerInteractor> { PlayerInteractorImpl(get()) }
}