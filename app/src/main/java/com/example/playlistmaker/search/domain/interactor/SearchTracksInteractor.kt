package com.example.playlistmaker.search.domain.interactor

import com.example.playlistmaker.search.domain.entity.Track
import com.example.playlistmaker.search.domain.repository.SearchRepository

interface SearchTracksInteractor {
    suspend fun execute(term: String): List<Track>
}

class SearchTracksInteractorImpl(
    private val searchRepository: SearchRepository
) : SearchTracksInteractor {
    override suspend fun execute(term: String): List<Track> {
        return if (term.isNotBlank()) {
            searchRepository.searchTracks(term)
        } else {
            emptyList()
        }
    }
}