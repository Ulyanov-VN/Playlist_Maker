package com.example.playlistmaker.data.db.playlist

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface PlaylistDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylist(playlist: PlaylistEntity): Long

    @Update
    suspend fun updatePlaylist(playlist: PlaylistEntity)

    @Query("SELECT * FROM playlists ORDER BY createdAt DESC")
    fun getAllPlaylists(): Flow<List<PlaylistEntity>>

    @Query("SELECT * FROM playlists WHERE playlistId = :playlistId")
    suspend fun getPlaylistById(playlistId: Long): PlaylistEntity?

    @Query("DELETE FROM playlists WHERE playlistId = :playlistId")
    suspend fun deletePlaylist(playlistId: Long)
}

@Dao
interface PlaylistTrackDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTrack(track: PlaylistTrackEntity)

    @Query("SELECT * FROM playlist_tracks WHERE trackId = :trackId")
    suspend fun getTrackById(trackId: Long): PlaylistTrackEntity?

    @Query("SELECT * FROM playlist_tracks WHERE trackId IN (:trackIds)")
    suspend fun getTracksByIds(trackIds: List<Long>): List<PlaylistTrackEntity>
}