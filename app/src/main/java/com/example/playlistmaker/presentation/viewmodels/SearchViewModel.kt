package com.example.playlistmaker.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.playlistmaker.domain.entity.Track
import com.example.playlistmaker.domain.interactor.ManageSearchHistoryInteractor
import com.example.playlistmaker.domain.interactor.SearchTracksInteractor
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SearchViewModel(
    private val searchTracksInteractor: SearchTracksInteractor,
    private val manageSearchHistoryInteractor: ManageSearchHistoryInteractor
) : ViewModel() {

    private val _state = MutableStateFlow<SearchUiState>(SearchUiState.Empty)
    val state: StateFlow<SearchUiState> = _state.asStateFlow()

    private var lastTerm: String? = null
    private var lastSearchResults: List<Track> = emptyList()

    fun search(term: String) {
        if (term.isBlank()) return

        lastTerm = term
        viewModelScope.launch {
            _state.value = SearchUiState.Loading
            try {
                val tracks = searchTracksInteractor.execute(term)
                lastSearchResults = tracks
                _state.value = when {
                    tracks.isNotEmpty() -> SearchUiState.Success(tracks)
                    else -> SearchUiState.NoResults
                }
            } catch (e: Exception) {
                _state.value = SearchUiState.Error
            }
        }
    }

    fun getHistory(): List<Track> {
        return manageSearchHistoryInteractor.getSearchHistory()
    }

    fun saveTrackToHistory(track: Track) {
        manageSearchHistoryInteractor.addTrackToHistory(track)
    }

    fun clearHistory() {
        manageSearchHistoryInteractor.clearSearchHistory()
    }

    fun retry() {
        lastTerm?.let { search(it) }
    }

    fun getLastSearchTerm(): String? = lastTerm

    fun getLastSearchResults(): List<Track> = lastSearchResults

    fun restoreLastSearch() {
        lastTerm?.let { term ->
            _state.value = when {
                lastSearchResults.isNotEmpty() -> SearchUiState.Success(lastSearchResults)
                else -> SearchUiState.Empty
            }
        }
    }

    fun clearState() {
        lastTerm = null
        _state.value = SearchUiState.Empty
    }
}