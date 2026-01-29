package com.example.playlistmaker.playlist.domain.interactor

import com.example.playlistmaker.playlist.domain.model.Playlist
import com.example.playlistmaker.playlist.domain.repository.PlaylistRepository
import com.example.playlistmaker.search.domain.entity.Track
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext

interface PlaylistInteractor {
    suspend fun createPlaylist(playlist: Playlist): Long
    suspend fun updatePlaylist(playlist: Playlist)
    fun getAllPlaylists(): Flow<List<Playlist>>
    suspend fun getPlaylistById(playlistId: Long): Playlist?
    suspend fun addTrackToPlaylist(playlist: Playlist, track: Track): Boolean
    suspend fun saveTrackToLibrary(track: Track)
}

class PlaylistInteractorImpl(
    private val repository: PlaylistRepository,
    private val ioContext: CoroutineContext
) : PlaylistInteractor {

    override suspend fun createPlaylist(playlist: Playlist): Long {
        return withContext(ioContext) {
            repository.createPlaylist(playlist)
        }
    }

    override suspend fun updatePlaylist(playlist: Playlist) {
        withContext(ioContext) {
            repository.updatePlaylist(playlist)
        }
    }

    override fun getAllPlaylists(): Flow<List<Playlist>> {
        return repository.getAllPlaylists()
    }

    override suspend fun getPlaylistById(playlistId: Long): Playlist? {
        return withContext(ioContext) {
            repository.getPlaylistById(playlistId)
        }
    }

    override suspend fun addTrackToPlaylist(playlist: Playlist, track: Track): Boolean {
        return withContext(ioContext) {
            try {
                repository.addTrackToPlaylist(playlist, track)
            } catch (e: Exception) {
                false
            }
        }
    }

    override suspend fun saveTrackToLibrary(track: Track) {
        withContext(ioContext) {
            repository.saveTrackToLibrary(track)
        }
    }
}