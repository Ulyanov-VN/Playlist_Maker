package com.example.playlistmaker.data.repository

import com.example.playlistmaker.network.ItunesApiService
import com.example.playlistmaker.network.SearchResponse

class SearchRepository(private val api: ItunesApiService) {
    suspend fun searchSongs(term: String): SearchResponse =
        api.searchSongs(term)
}