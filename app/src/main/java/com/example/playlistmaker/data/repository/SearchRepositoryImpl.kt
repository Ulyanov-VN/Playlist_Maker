package com.example.playlistmaker.data.repository

import com.example.playlistmaker.data.mapper.TrackMapper
import com.example.playlistmaker.domain.entity.Track
import com.example.playlistmaker.domain.repository.SearchRepository
import com.example.playlistmaker.data.network.ItunesApiService


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