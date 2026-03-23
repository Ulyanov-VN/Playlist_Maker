package com.example.playlistmaker.di

import com.example.playlistmaker.favorites.data.repository.FavoriteTracksRepositoryImpl
import com.example.playlistmaker.favorites.domain.repository.FavoriteTracksRepository
import com.example.playlistmaker.player.data.repository.MediaRepositoryImpl
import com.example.playlistmaker.player.domain.repository.MediaRepository
import com.example.playlistmaker.playlist.data.repository.PlaylistRepositoryImpl
import com.example.playlistmaker.playlist.domain.repository.PlaylistRepository
import com.example.playlistmaker.search.data.repository.SearchRepositoryImpl
import com.example.playlistmaker.search.domain.repository.SearchRepository
import org.koin.dsl.module

val repositoryModule = module {
    single<SearchRepository> {
        SearchRepositoryImpl(
            api = get(),
            trackMapper = get(),
            favoriteTracksDao = get()
        )
    }

    single<PlaylistRepository> {
        PlaylistRepositoryImpl(
            playlistDao = get(),
            playlistTrackDao = get(),
            gson = get()
        )
    }

    single<MediaRepository> { MediaRepositoryImpl() }

    single<FavoriteTracksRepository> {
        FavoriteTracksRepositoryImpl(
            dao = get()
        )
    }
}