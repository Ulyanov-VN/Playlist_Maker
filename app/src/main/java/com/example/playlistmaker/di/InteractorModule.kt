package com.example.playlistmaker.di

import com.example.playlistmaker.favorites.domain.interactor.FavoriteTracksInteractor
import com.example.playlistmaker.favorites.domain.interactor.FavoriteTracksInteractorImpl
import com.example.playlistmaker.media.domain.interactor.ManageSearchHistoryInteractor
import com.example.playlistmaker.media.domain.interactor.ManageSearchHistoryInteractorImpl
import com.example.playlistmaker.player.domain.interactor.*
import com.example.playlistmaker.playlist.domain.interactor.PlaylistInteractor
import com.example.playlistmaker.playlist.domain.interactor.PlaylistInteractorImpl
import com.example.playlistmaker.playlist.domain.repository.PlaylistRepository
import com.example.playlistmaker.search.domain.interactor.SearchTracksInteractor
import com.example.playlistmaker.search.domain.interactor.SearchTracksInteractorImpl
import com.example.playlistmaker.settings.domain.interactor.ManageThemeInteractor
import com.example.playlistmaker.settings.domain.interactor.ManageThemeInteractorImpl
import com.example.playlistmaker.sharing.domain.interactor.SharingInteractor
import com.example.playlistmaker.sharing.domain.interactor.SharingInteractorImpl
import kotlinx.coroutines.Dispatchers
import org.koin.dsl.module

val interactorModule = module {
    // Existing interactors
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

    // Playlist interactor with IO dispatcher
    single<PlaylistInteractor> {
        PlaylistInteractorImpl(
            repository = get<PlaylistRepository>(),
            ioContext = Dispatchers.IO
        )
    }

    // Favorites
    single<FavoriteTracksInteractor> {
        FavoriteTracksInteractorImpl(
            repository = get()
        )
    }
}