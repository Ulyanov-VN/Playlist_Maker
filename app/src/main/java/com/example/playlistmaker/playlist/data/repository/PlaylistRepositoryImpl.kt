package com.example.playlistmaker.playlist.data.repository

import android.util.Log
import com.example.playlistmaker.data.db.playlist.PlaylistDao
import com.example.playlistmaker.data.db.playlist.PlaylistEntity
import com.example.playlistmaker.data.db.playlist.PlaylistTrackDao
import com.example.playlistmaker.data.db.playlist.PlaylistTrackEntity
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
                val entity = PlaylistEntity.fromPlaylist(playlist, gson)
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
                val entity = PlaylistEntity.fromPlaylist(playlist, gson)
                playlistDao.updatePlaylist(entity)
            } catch (e: Exception) {
                Log.e(TAG, "Error updating playlist", e)
            }
        }
    }

    override fun getAllPlaylists(): Flow<List<Playlist>> {
        return playlistDao.getAllPlaylists()
            .map { entities -> entities.map { it.toPlaylist(gson) } }
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
                if (playlist.trackIds.contains(track.trackId)) return@withContext false

                // Сохраняем трек в общую таблицу
                try {
                    playlistTrackDao.insertTrack(PlaylistTrackEntity.fromTrack(track))
                } catch (e: Exception) {
                    Log.d(TAG, "Track insert ignored (already exists)")
                }

                val updatedTrackIds = playlist.trackIds.toMutableList().apply { add(track.trackId) }
                val updatedPlaylist = playlist.copy(
                    trackIds = updatedTrackIds,
                    trackCount = updatedTrackIds.size
                )

                playlistDao.updatePlaylist(PlaylistEntity.fromPlaylist(updatedPlaylist, gson))
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
                playlistTrackDao.insertTrack(PlaylistTrackEntity.fromTrack(track))
            } catch (e: Exception) {
                Log.d(TAG, "Track insert ignored (already exists)")
            }
        }
    }

    override suspend fun getTracksByIds(trackIds: List<Long>): List<Track> {
        return withContext(Dispatchers.IO) {
            try {
                if (trackIds.isEmpty()) return@withContext emptyList()
                playlistTrackDao.getTracksByIds(trackIds).map { it.toTrack() }
            } catch (e: Exception) {
                Log.e(TAG, "Error getting tracks by ids", e)
                emptyList()
            }
        }
    }

    override suspend fun removeTrackFromPlaylist(playlistId: Long, trackId: Long) {
        withContext(Dispatchers.IO) {
            try {
                val playlistEntity = playlistDao.getPlaylistById(playlistId) ?: return@withContext
                val playlist = playlistEntity.toPlaylist(gson)

                val updatedIds = playlist.trackIds.filterNot { it == trackId }
                val updatedPlaylist = playlist.copy(
                    trackIds = updatedIds,
                    trackCount = updatedIds.size
                )

                playlistDao.updatePlaylist(PlaylistEntity.fromPlaylist(updatedPlaylist, gson))
                cleanupOrphanTrack(trackId)
            } catch (e: Exception) {
                Log.e(TAG, "Error removing track from playlist", e)
            }
        }
    }

    // ✅ Шаг 4: удаление плейлиста + чистка треков, которые больше нигде не используются
    override suspend fun deletePlaylist(playlistId: Long) {
        withContext(Dispatchers.IO) {
            try {
                val playlistEntity = playlistDao.getPlaylistById(playlistId) ?: return@withContext
                val playlist = playlistEntity.toPlaylist(gson)

                // удаляем плейлист
                playlistDao.deletePlaylist(playlistId)

                // чистим сироты: каждый трек из удалённого плейлиста проверяем
                for (trackId in playlist.trackIds) {
                    cleanupOrphanTrack(trackId)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error deleting playlist", e)
            }
        }
    }

    private suspend fun cleanupOrphanTrack(trackId: Long) {
        try {
            val allPlaylists = playlistDao.getAllPlaylistsOnce().map { it.toPlaylist(gson) }
            val usedSomewhere = allPlaylists.any { it.trackIds.contains(trackId) }
            if (!usedSomewhere) {
                playlistTrackDao.deleteTrackById(trackId)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error cleanup orphan track", e)
        }
    }
}
