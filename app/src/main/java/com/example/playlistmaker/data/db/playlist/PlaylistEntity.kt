package com.example.playlistmaker.data.db.playlist

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.playlistmaker.playlist.domain.model.Playlist
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

@Entity(tableName = "playlists")
data class PlaylistEntity(
    @PrimaryKey(autoGenerate = true)
    val playlistId: Long = 0,
    val name: String,
    val description: String?,
    val coverImagePath: String?,
    val trackIdsJson: String, // JSON-строка с List<Long>
    val trackCount: Int,
    val createdAt: Long = System.currentTimeMillis()
) {
    fun toPlaylist(gson: Gson): Playlist {
        val type = object : TypeToken<List<Long>>() {}.type
        val trackIds = gson.fromJson<List<Long>>(trackIdsJson, type) ?: emptyList()

        return Playlist(
            playlistId = playlistId,
            name = name,
            description = description,
            coverImagePath = coverImagePath,
            trackIds = trackIds,
            trackCount = trackCount,
            createdAt = createdAt
        )
    }

    companion object {
        fun fromPlaylist(playlist: Playlist, gson: Gson): PlaylistEntity {
            val trackIdsJson = gson.toJson(playlist.trackIds)

            return PlaylistEntity(
                playlistId = playlist.playlistId,
                name = playlist.name,
                description = playlist.description,
                coverImagePath = playlist.coverImagePath,
                trackIdsJson = trackIdsJson,
                trackCount = playlist.trackCount,
                createdAt = playlist.createdAt
            )
        }
    }
}