package com.example.playlistmaker.media.domain.interactor

import com.example.playlistmaker.media.domain.repository.HistoryRepository
import com.example.playlistmaker.search.domain.entity.Track

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