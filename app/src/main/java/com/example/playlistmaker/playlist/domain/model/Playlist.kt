package com.example.playlistmaker.playlist.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Playlist(
    val playlistId: Long = 0,
    val name: String,
    val description: String?,
    val coverImagePath: String?,
    val trackIds: List<Long>,
    val trackCount: Int,
    val createdAt: Long = System.currentTimeMillis()
) : Parcelable