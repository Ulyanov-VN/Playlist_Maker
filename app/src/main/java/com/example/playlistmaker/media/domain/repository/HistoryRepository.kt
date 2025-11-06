package com.example.playlistmaker.media.domain.repository

import com.example.playlistmaker.search.domain.entity.Track

interface HistoryRepository {
    fun getHistory(): List<Track>
    fun saveTrack(track: Track)
    fun clearHistory()
}