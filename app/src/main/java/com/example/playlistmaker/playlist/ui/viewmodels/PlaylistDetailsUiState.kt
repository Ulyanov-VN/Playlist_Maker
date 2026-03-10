package com.example.playlistmaker.playlist.ui.viewmodels

import com.example.playlistmaker.search.domain.entity.Track

data class PlaylistDetailsUiState(
    val name: String,
    val description: String?,
    val coverPath: String?,
    val totalMinutesText: String,
    val trackCountText: String,
    val tracks: List<Track>
)
