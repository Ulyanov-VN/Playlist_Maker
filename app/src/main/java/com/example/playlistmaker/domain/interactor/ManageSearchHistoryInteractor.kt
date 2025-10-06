package com.example.playlistmaker.domain.interactor

import com.example.playlistmaker.domain.entity.Track
import com.example.playlistmaker.domain.repository.HistoryRepository

interface ManageSearchHistoryInteractor {
    fun getSearchHistory(): List<Track>
    fun addTrackToHistory(track: Track)
    fun clearSearchHistory()
}

class ManageSearchHistoryInteractorImpl(
    private val historyRepository: HistoryRepository
) : ManageSearchHistoryInteractor {
    override fun getSearchHistory(): List<Track> = historyRepository.getHistory()
    override fun addTrackToHistory(track: Track) = historyRepository.saveTrack(track)
    override fun clearSearchHistory() = historyRepository.clearHistory()
}