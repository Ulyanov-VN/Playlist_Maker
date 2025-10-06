package com.example.playlistmaker.domain.repository

import com.example.playlistmaker.domain.entity.Track

interface HistoryRepository {
    fun getHistory(): List<Track>
    fun saveTrack(track: Track)
    fun clearHistory()
}