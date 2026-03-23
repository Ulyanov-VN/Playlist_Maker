package com.example.playlistmaker.search.domain.interactor

import com.example.playlistmaker.search.domain.entity.Track
import com.example.playlistmaker.search.domain.repository.SearchRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

interface SearchTracksInteractor {
    fun execute(term: String): Flow<List<Track>>
}

class SearchTracksInteractorImpl(
    private val searchRepository: SearchRepository
) : SearchTracksInteractor {

    override fun execute(term: String): Flow<List<Track>> {
        return if (term.isNotBlank()) {
            searchRepository.searchTracks(term)
        } else {
            flowOf(emptyList())
        }
    }
}
