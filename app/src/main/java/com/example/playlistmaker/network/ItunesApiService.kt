package com.example.playlistmaker.network

import com.example.playlistmaker.Track
import retrofit2.http.GET
import retrofit2.http.Query

data class SearchResponse(
    val resultCount: Int,
    val results: List<Track>
)

interface ItunesApiService {
    @GET("search?entity=song")
    suspend fun searchSongs(@Query("term") term: String): SearchResponse
}