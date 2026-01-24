package com.example.playlistmaker.search.data.repository

import com.example.playlistmaker.search.data.mapper.TrackMapper
import com.example.playlistmaker.search.data.network.ItunesApiService
import com.example.playlistmaker.search.domain.entity.Track
import com.example.playlistmaker.search.domain.repository.SearchRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

class SearchRepositoryImpl(
    private val api: ItunesApiService,
    private val trackMapper: TrackMapper
) : SearchRepository {

    override fun searchTracks(term: String): Flow<List<Track>> = flow {
        val response = api.searchSongs(term)
        val tracks = response.results.map { dto -> trackMapper.mapDtoToEntity(dto) }
        emit(tracks)
    }.flowOn(Dispatchers.IO)
}
