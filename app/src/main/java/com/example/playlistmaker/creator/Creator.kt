package com.example.playlistmaker.creator

import android.content.Context
import com.example.playlistmaker.data.mapper.TrackMapper
import com.example.playlistmaker.data.network.RetrofitInstance
import com.example.playlistmaker.data.repository.HistoryRepositoryImpl
import com.example.playlistmaker.data.repository.SearchRepositoryImpl
import com.example.playlistmaker.data.repository.SettingsRepositoryImpl
import com.example.playlistmaker.domain.interactor.FormatTimeInteractor
import com.example.playlistmaker.domain.interactor.GetCountryNameInteractor
import com.example.playlistmaker.domain.interactor.GetCoverArtworkInteractor
import com.example.playlistmaker.domain.interactor.GetReleaseYearInteractor
import com.example.playlistmaker.domain.interactor.ManageSearchHistoryInteractor
import com.example.playlistmaker.domain.interactor.ManageSearchHistoryInteractorImpl
import com.example.playlistmaker.domain.interactor.ManageThemeInteractor
import com.example.playlistmaker.domain.interactor.ManageThemeInteractorImpl
import com.example.playlistmaker.domain.interactor.PlayerInteractor
import com.example.playlistmaker.domain.interactor.PlayerInteractorImpl
import com.example.playlistmaker.domain.interactor.SearchTracksInteractor
import com.example.playlistmaker.domain.interactor.SearchTracksInteractorImpl
import com.example.playlistmaker.domain.repository.HistoryRepository
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
        return FormatTimeInteractor()
    }

    fun provideGetCountryNameInteractor(): GetCountryNameInteractor {
        return GetCountryNameInteractor()
    }

    fun provideGetCoverArtworkInteractor(): GetCoverArtworkInteractor {
        return GetCoverArtworkInteractor()
    }

    fun provideGetReleaseYearInteractor(): GetReleaseYearInteractor {
        return GetReleaseYearInteractor()
    }

    fun providePlayerInteractor(): PlayerInteractor {
        return PlayerInteractorImpl()
    }
}