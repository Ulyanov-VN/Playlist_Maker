package com.example.playlistmaker.search.domain.repository

import com.example.playlistmaker.search.domain.entity.Track

interface SearchRepository {
    suspend fun searchTracks(term: String): List<Track>
}