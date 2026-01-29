package com.example.playlistmaker.di

import android.content.Context
import android.content.SharedPreferences
import com.example.playlistmaker.data.db.AppDatabase
import com.example.playlistmaker.media.data.repository.HistoryRepositoryImpl
import com.example.playlistmaker.media.domain.repository.HistoryRepository
import com.example.playlistmaker.search.data.mapper.TrackMapper
import com.example.playlistmaker.search.data.network.ItunesApiService
import com.example.playlistmaker.settings.data.repository.SettingsRepositoryImpl
import com.example.playlistmaker.settings.domain.repository.SettingsRepository
import com.example.playlistmaker.sharing.data.repository.SharingRepositoryImpl
import com.example.playlistmaker.sharing.domain.repository.SharingRepository
import com.google.gson.Gson
import org.koin.android.ext.koin.androidContext
import org.koin.core.qualifier.named
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

val dataModule = module {

    // Gson
    single { Gson() }

    single {
        get<AppDatabase>().playlistDao()
    }
    single {
        get<AppDatabase>().playlistTrackDao()
    }

    // SharedPreferences
    single<SharedPreferences>(named("history_prefs")) {
        androidContext().getSharedPreferences("search_history_prefs", Context.MODE_PRIVATE)
    }
    single<SharedPreferences>(named("theme_prefs")) {
        androidContext().getSharedPreferences("theme_prefs", Context.MODE_PRIVATE)
    }

    // Database
    single {
        AppDatabase.buildDatabase(androidContext())
    }
    single {
        get<AppDatabase>().favoriteTracksDao()
    }

    // Retrofit + iTunes API
    single {
        Retrofit.Builder()
            .baseUrl("https://itunes.apple.com/")
            .addConverterFactory(GsonConverterFactory.create(get()))
            .build()
    }
    single<ItunesApiService> {
        get<Retrofit>().create(ItunesApiService::class.java)
    }

    // Mapper
    factory { TrackMapper() }

    // Repositories (Data)
    single<HistoryRepository> { HistoryRepositoryImpl(get(named("history_prefs")), get()) }
    single<SettingsRepository> { SettingsRepositoryImpl(get(named("theme_prefs"))) }
    single<SharingRepository> { SharingRepositoryImpl(androidContext()) }
}