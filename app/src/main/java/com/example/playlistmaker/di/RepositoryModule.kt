package com.example.playlistmaker.di

import com.example.playlistmaker.player.data.repository.MediaRepositoryImpl
import com.example.playlistmaker.player.domain.repository.MediaRepository
import com.example.playlistmaker.search.data.repository.SearchRepositoryImpl
import com.example.playlistmaker.search.domain.repository.SearchRepository
import org.koin.dsl.module

val repositoryModule = module {
    single<SearchRepository> { SearchRepositoryImpl(api = get(), trackMapper = get()) }
    single<MediaRepository> { MediaRepositoryImpl() }
}
