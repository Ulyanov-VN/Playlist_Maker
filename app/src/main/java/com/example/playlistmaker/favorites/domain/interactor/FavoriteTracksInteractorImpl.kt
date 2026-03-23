package com.example.playlistmaker.favorites.domain.interactor

import com.example.playlistmaker.favorites.domain.repository.FavoriteTracksRepository
import com.example.playlistmaker.search.domain.entity.Track
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class FavoriteTracksInteractorImpl(
    private val repository: FavoriteTracksRepository
) : FavoriteTracksInteractor {

    override suspend fun addToFavorites(track: Track) {
        repository.addToFavorites(track)
    }

    override suspend fun removeFromFavorites(track: Track) {
        repository.removeFromFavorites(track)
    }

    override fun getAllFavorites(): Flow<List<Track>> {
        return repository.getAllFavorites()
    }

    override suspend fun checkIsFavorite(track: Track): Boolean {
        val favoriteIds = repository.getAllFavoriteIds()
        return favoriteIds.contains(track.trackId)
    }

    override suspend fun updateFavoriteStatus(tracks: List<Track>): List<Track> {
        val favoriteIds = repository.getAllFavoriteIds()
        return tracks.map { track ->
            track.copy(isFavorite = favoriteIds.contains(track.trackId))
        }
    }
}