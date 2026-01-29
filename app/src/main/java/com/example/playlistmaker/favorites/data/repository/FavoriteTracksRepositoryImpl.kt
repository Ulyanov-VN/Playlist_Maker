package com.example.playlistmaker.favorites.data.repository

import com.example.playlistmaker.data.db.favorite.FavoriteTrackEntity
import com.example.playlistmaker.data.db.favorite.FavoriteTracksDao
import com.example.playlistmaker.favorites.domain.repository.FavoriteTracksRepository
import com.example.playlistmaker.search.domain.entity.Track
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class FavoriteTracksRepositoryImpl(
    private val dao: FavoriteTracksDao
) : FavoriteTracksRepository {

    override suspend fun addToFavorites(track: Track) {
        dao.insert(FavoriteTrackEntity.fromTrack(track))
    }

    override suspend fun removeFromFavorites(track: Track) {
        dao.delete(FavoriteTrackEntity.fromTrack(track))
    }

    override fun getAllFavorites(): Flow<List<Track>> {
        return dao.getAllFavorites().map { entities ->
            entities.map { it.toTrack() }
        }
    }

    override suspend fun getAllFavoriteIds(): List<Long> {
        return dao.getAllFavoriteIds()
    }
}