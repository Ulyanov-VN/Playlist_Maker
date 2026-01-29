package com.example.playlistmaker.favorites.domain.interactor

import com.example.playlistmaker.search.domain.entity.Track
import kotlinx.coroutines.flow.Flow

interface FavoriteTracksInteractor {
    suspend fun addToFavorites(track: Track)
    suspend fun removeFromFavorites(track: Track)
    fun getAllFavorites(): Flow<List<Track>>
    suspend fun checkIsFavorite(track: Track): Boolean
    suspend fun updateFavoriteStatus(tracks: List<Track>): List<Track>
}