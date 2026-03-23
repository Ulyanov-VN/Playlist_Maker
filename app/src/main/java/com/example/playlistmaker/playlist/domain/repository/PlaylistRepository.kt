package com.example.playlistmaker.playlist.domain.repository

import com.example.playlistmaker.playlist.domain.model.Playlist
import com.example.playlistmaker.search.domain.entity.Track
import kotlinx.coroutines.flow.Flow

interface PlaylistRepository {
    suspend fun createPlaylist(playlist: Playlist): Long
    suspend fun updatePlaylist(playlist: Playlist)
    fun getAllPlaylists(): Flow<List<Playlist>>
    suspend fun getPlaylistById(playlistId: Long): Playlist?

    suspend fun addTrackToPlaylist(playlist: Playlist, track: Track): Boolean
    suspend fun saveTrackToLibrary(track: Track)

    suspend fun getTracksByIds(trackIds: List<Long>): List<Track>

    suspend fun removeTrackFromPlaylist(playlistId: Long, trackId: Long)

    suspend fun deletePlaylist(playlistId: Long)
}
