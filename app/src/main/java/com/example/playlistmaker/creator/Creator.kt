package com.example.playlistmaker.di

import android.content.Context
import com.example.playlistmaker.data.mapper.TrackMapper
import com.example.playlistmaker.data.network.RetrofitInstance
import com.example.playlistmaker.data.repository.HistoryRepositoryImpl
import com.example.playlistmaker.data.repository.MediaRepositoryImpl
import com.example.playlistmaker.data.repository.SearchRepositoryImpl
import com.example.playlistmaker.data.repository.SettingsRepositoryImpl
import com.example.playlistmaker.domain.interactor.*
import com.example.playlistmaker.domain.repository.HistoryRepository
import com.example.playlistmaker.domain.repository.MediaRepository
import com.example.playlistmaker.domain.repository.SearchRepository
import com.example.playlistmaker.domain.repository.SettingsRepository

object Creator {

    private fun provideSearchRepository(): SearchRepository {
        return SearchRepositoryImpl(
            api = RetrofitInstance.api,
            trackMapper = TrackMapper()
        )
    }

    private fun provideHistoryRepository(context: Context): HistoryRepository {
        return HistoryRepositoryImpl(
            prefs = context.getSharedPreferences("search_history_prefs", Context.MODE_PRIVATE)
        )
    }

    private fun provideSettingsRepository(context: Context): SettingsRepository {
        return SettingsRepositoryImpl(
            prefs = context.getSharedPreferences("theme_prefs", Context.MODE_PRIVATE)
        )
    }


    private fun provideMediaRepository(): MediaRepository {
        return MediaRepositoryImpl()
    }

    fun provideSearchTracksInteractor(): SearchTracksInteractor {
        return SearchTracksInteractorImpl(provideSearchRepository())
    }

    fun provideManageSearchHistoryInteractor(context: Context): ManageSearchHistoryInteractor {
        return ManageSearchHistoryInteractorImpl(provideHistoryRepository(context))
    }

    fun provideManageThemeInteractor(context: Context): ManageThemeInteractor {
        return ManageThemeInteractorImpl(provideSettingsRepository(context))
    }

    fun provideFormatTimeInteractor(): FormatTimeInteractor {
        return FormatTimeInteractorImpl()
    }

    fun provideGetCountryNameInteractor(): GetCountryNameInteractor {
        return GetCountryNameInteractorImpl()
    }

    fun provideGetCoverArtworkInteractor(): GetCoverArtworkInteractor {
        return GetCoverArtworkInteractorImpl()
    }

    fun provideGetReleaseYearInteractor(): GetReleaseYearInteractor {
        return GetReleaseYearInteractorImpl()
    }

    fun providePlayerInteractor(): PlayerInteractor {
        return PlayerInteractorImpl(provideMediaRepository())
    }
}