package com.example.playlistmaker.search.data.repository

import com.example.playlistmaker.search.data.mapper.TrackMapper
import com.example.playlistmaker.search.data.network.ItunesApiService
import com.example.playlistmaker.search.domain.entity.Track
import com.example.playlistmaker.search.domain.repository.SearchRepository

class SearchRepositoryImpl(
    private val api: ItunesApiService,
    private val trackMapper: TrackMapper
) : SearchRepository {

    override suspend fun searchTracks(term: String): List<Track> {
        val response = api.searchSongs(term)
        return response.results.map { trackDto ->
            trackMapper.mapDtoToEntity(trackDto)
        }
    }
}