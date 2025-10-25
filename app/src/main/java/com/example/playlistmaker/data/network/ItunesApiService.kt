package com.example.playlistmaker.data.network

import com.example.playlistmaker.data.dto.TrackDto
import retrofit2.http.GET
import retrofit2.http.Query

data class SearchResponse(
    val resultCount: Int,
    val results: List<TrackDto>
)

interface ItunesApiService {
    @GET("search?entity=song")
    suspend fun searchSongs(@Query("term") term: String): SearchResponse
}