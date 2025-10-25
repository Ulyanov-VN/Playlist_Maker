package com.example.playlistmaker.creator

import android.content.Context
import com.example.playlistmaker.media.data.repository.HistoryRepositoryImpl
import com.example.playlistmaker.media.domain.interactor.ManageSearchHistoryInteractor
import com.example.playlistmaker.media.domain.interactor.ManageSearchHistoryInteractorImpl
import com.example.playlistmaker.media.domain.repository.HistoryRepository
import com.example.playlistmaker.player.data.repository.MediaRepositoryImpl
import com.example.playlistmaker.player.domain.interactor.*
import com.example.playlistmaker.player.domain.repository.MediaRepository
import com.example.playlistmaker.search.data.mapper.TrackMapper
import com.example.playlistmaker.search.data.network.RetrofitInstance
import com.example.playlistmaker.search.data.repository.SearchRepositoryImpl
import com.example.playlistmaker.search.domain.interactor.SearchTracksInteractor
import com.example.playlistmaker.search.domain.interactor.SearchTracksInteractorImpl
import com.example.playlistmaker.search.domain.repository.SearchRepository
import com.example.playlistmaker.settings.data.repository.SettingsRepositoryImpl
import com.example.playlistmaker.settings.domain.interactor.ManageThemeInteractor
import com.example.playlistmaker.settings.domain.interactor.ManageThemeInteractorImpl
import com.example.playlistmaker.settings.domain.repository.SettingsRepository
import com.example.playlistmaker.sharing.domain.interactor.SharingInteractor
import com.example.playlistmaker.sharing.domain.interactor.SharingInteractorImpl

object Creator {


    private fun provideSearchRepository(): SearchRepository {
        return SearchRepositoryImpl(
            api = RetrofitInstance.api,
            trackMapper = TrackMapper()
        )
    }

    fun provideSearchTracksInteractor(): SearchTracksInteractor {
        return SearchTracksInteractorImpl(provideSearchRepository())
    }


    private fun provideHistoryRepository(context: Context): HistoryRepository {
        return HistoryRepositoryImpl(
            prefs = context.getSharedPreferences("search_history_prefs", Context.MODE_PRIVATE)
        )
    }

    fun provideManageSearchHistoryInteractor(context: Context): ManageSearchHistoryInteractor {
        return ManageSearchHistoryInteractorImpl(provideHistoryRepository(context))
    }


    private fun provideSettingsRepository(context: Context): SettingsRepository {
        return SettingsRepositoryImpl(
            prefs = context.getSharedPreferences("theme_prefs", Context.MODE_PRIVATE)
        )
    }

    fun provideManageThemeInteractor(context: Context): ManageThemeInteractor {
        return ManageThemeInteractorImpl(provideSettingsRepository(context))
    }


    private fun provideMediaRepository(): MediaRepository {
        return MediaRepositoryImpl()
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


    fun provideSharingInteractor(): SharingInteractor {
        return SharingInteractorImpl()
    }
}