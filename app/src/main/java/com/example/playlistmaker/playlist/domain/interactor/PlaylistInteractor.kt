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

    suspend fun getTracksByIds(trackIds: List<Long>): List<Track>
    suspend fun removeTrackFromPlaylist(playlistId: Long, trackId: Long)

    // ✅ Шаг 4
    suspend fun deletePlaylist(playlistId: Long)
}

class PlaylistInteractorImpl(
    private val repository: PlaylistRepository,
    private val ioContext: CoroutineContext
) : PlaylistInteractor {

    override suspend fun createPlaylist(playlist: Playlist): Long =
        withContext(ioContext) { repository.createPlaylist(playlist) }

    override suspend fun updatePlaylist(playlist: Playlist) =
        withContext(ioContext) { repository.updatePlaylist(playlist) }

    override fun getAllPlaylists(): Flow<List<Playlist>> = repository.getAllPlaylists()

    override suspend fun getPlaylistById(playlistId: Long): Playlist? =
        withContext(ioContext) { repository.getPlaylistById(playlistId) }

    override suspend fun getTracksByIds(trackIds: List<Long>): List<Track> =
        withContext(ioContext) { repository.getTracksByIds(trackIds) }

    override suspend fun addTrackToPlaylist(playlist: Playlist, track: Track): Boolean =
        withContext(ioContext) { repository.addTrackToPlaylist(playlist, track) }

    override suspend fun saveTrackToLibrary(track: Track) =
        withContext(ioContext) { repository.saveTrackToLibrary(track) }

    override suspend fun removeTrackFromPlaylist(playlistId: Long, trackId: Long) =
        withContext(ioContext) { repository.removeTrackFromPlaylist(playlistId, trackId) }

    override suspend fun deletePlaylist(playlistId: Long) =
        withContext(ioContext) { repository.deletePlaylist(playlistId) }
}
