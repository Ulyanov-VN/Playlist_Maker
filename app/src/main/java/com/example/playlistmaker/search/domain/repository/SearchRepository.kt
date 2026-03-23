package com.example.playlistmaker.search.domain.repository

import com.example.playlistmaker.search.domain.entity.Track
import kotlinx.coroutines.flow.Flow

interface SearchRepository {
    fun searchTracks(term: String): Flow<List<Track>>
}
