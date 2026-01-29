
package com.example.playlistmaker.data.db.favorite

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.playlistmaker.search.domain.entity.Track

@Entity(tableName = "favorite_tracks")
data class FavoriteTrackEntity(
    @PrimaryKey
    val trackId: Long,
    val trackName: String?,
    val artistName: String?,
    val collectionName: String?,
    val releaseDate: String?,
    val primaryGenreName: String?,
    val country: String?,
    val trackTimeMillis: Long?,
    val artworkUrl100: String?,
    val previewUrl: String?,
    val addedAt: Long = System.currentTimeMillis()
) {
    fun toTrack(): Track = Track(
        trackId = trackId,
        trackName = trackName,
        artistName = artistName,
        collectionName = collectionName,
        releaseDate = releaseDate,
        primaryGenreName = primaryGenreName,
        country = country,
        trackTimeMillis = trackTimeMillis,
        artworkUrl100 = artworkUrl100,
        previewUrl = previewUrl
    )

    companion object {
        fun fromTrack(track: Track): FavoriteTrackEntity = FavoriteTrackEntity(
            trackId = track.trackId,
            trackName = track.trackName,
            artistName = track.artistName,
            collectionName = track.collectionName,
            releaseDate = track.releaseDate,
            primaryGenreName = track.primaryGenreName,
            country = track.country,
            trackTimeMillis = track.trackTimeMillis,
            artworkUrl100 = track.artworkUrl100,
            previewUrl = track.previewUrl,
            addedAt = System.currentTimeMillis()
        )
    }
}