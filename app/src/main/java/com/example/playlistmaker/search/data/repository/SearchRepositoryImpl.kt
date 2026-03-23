package com.example.playlistmaker.search.data.repository

import com.example.playlistmaker.data.db.favorite.FavoriteTracksDao
import com.example.playlistmaker.search.data.mapper.TrackMapper
import com.example.playlistmaker.search.data.network.ItunesApiService
import com.example.playlistmaker.search.domain.entity.Track
import com.example.playlistmaker.search.domain.repository.SearchRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext

class SearchRepositoryImpl(
    private val api: ItunesApiService,
    private val trackMapper: TrackMapper,
    private val favoriteTracksDao: FavoriteTracksDao
) : SearchRepository {

    override fun searchTracks(term: String): Flow<List<Track>> = flow {
        val response = api.searchSongs(term)
        val tracks = response.results.map { dto -> trackMapper.mapDtoToEntity(dto) }
        val tracksWithFavoriteStatus = withContext(Dispatchers.IO) {
            updateTracksWithFavoriteStatus(tracks)
        }
        emit(tracksWithFavoriteStatus)
    }.flowOn(Dispatchers.IO)

    private suspend fun updateTracksWithFavoriteStatus(tracks: List<Track>): List<Track> {
        val favoriteIds = favoriteTracksDao.getAllFavoriteIds()
        return tracks.map { track ->
            track.copy(isFavorite = favoriteIds.contains(track.trackId))
        }
    }
}