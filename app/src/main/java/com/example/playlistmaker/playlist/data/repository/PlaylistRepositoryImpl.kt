package com.example.playlistmaker.playlist.data.repository

import android.util.Log
import com.example.playlistmaker.data.db.playlist.PlaylistDao
import com.example.playlistmaker.data.db.playlist.PlaylistTrackDao
import com.example.playlistmaker.playlist.domain.model.Playlist
import com.example.playlistmaker.playlist.domain.repository.PlaylistRepository
import com.example.playlistmaker.search.domain.entity.Track
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class PlaylistRepositoryImpl(
    private val playlistDao: PlaylistDao,
    private val playlistTrackDao: PlaylistTrackDao,
    private val gson: Gson
) : PlaylistRepository {

    companion object {
        private const val TAG = "PlaylistRepository"
    }

    override suspend fun createPlaylist(playlist: Playlist): Long {
        return withContext(Dispatchers.IO) {
            try {
                val entity = com.example.playlistmaker.data.db.playlist.PlaylistEntity.fromPlaylist(playlist, gson)
                playlistDao.insertPlaylist(entity)
            } catch (e: Exception) {
                Log.e(TAG, "Error creating playlist", e)
                -1L
            }
        }
    }

    override suspend fun updatePlaylist(playlist: Playlist) {
        withContext(Dispatchers.IO) {
            try {
                val entity = com.example.playlistmaker.data.db.playlist.PlaylistEntity.fromPlaylist(playlist, gson)
                playlistDao.updatePlaylist(entity)
            } catch (e: Exception) {
                Log.e(TAG, "Error updating playlist", e)
            }
        }
    }

    override fun getAllPlaylists(): Flow<List<Playlist>> {
        return playlistDao.getAllPlaylists()
            .map { entities ->
                entities.map { it.toPlaylist(gson) }
            }
    }

    override suspend fun getPlaylistById(playlistId: Long): Playlist? {
        return withContext(Dispatchers.IO) {
            try {
                playlistDao.getPlaylistById(playlistId)?.toPlaylist(gson)
            } catch (e: Exception) {
                Log.e(TAG, "Error getting playlist by id", e)
                null
            }
        }
    }

    override suspend fun addTrackToPlaylist(playlist: Playlist, track: Track): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                // Проверяем, есть ли трек уже в плейлисте
                if (playlist.trackIds.contains(track.trackId)) {
                    return@withContext false
                }

                // Сохраняем трек в таблицу треков
                try {
                    playlistTrackDao.insertTrack(
                        com.example.playlistmaker.data.db.playlist.PlaylistTrackEntity.fromTrack(track)
                    )
                } catch (e: Exception) {
                    // Игнорируем ошибку дублирования
                    Log.d(TAG, "Track already exists in library")
                }

                // Обновляем список ID треков в плейлисте
                val updatedTrackIds = playlist.trackIds.toMutableList().apply {
                    add(track.trackId)
                }
                val updatedPlaylist = playlist.copy(
                    trackIds = updatedTrackIds,
                    trackCount = updatedTrackIds.size
                )

                val entity = com.example.playlistmaker.data.db.playlist.PlaylistEntity.fromPlaylist(updatedPlaylist, gson)
                playlistDao.updatePlaylist(entity)

                true
            } catch (e: Exception) {
                Log.e(TAG, "Error adding track to playlist", e)
                false
            }
        }
    }

    override suspend fun saveTrackToLibrary(track: Track) {
        withContext(Dispatchers.IO) {
            try {
                playlistTrackDao.insertTrack(
                    com.example.playlistmaker.data.db.playlist.PlaylistTrackEntity.fromTrack(track)
                )
            } catch (e: Exception) {
                // Игнорируем ошибку дублирования
                Log.d(TAG, "Track already exists in library")
            }
        }
    }
}