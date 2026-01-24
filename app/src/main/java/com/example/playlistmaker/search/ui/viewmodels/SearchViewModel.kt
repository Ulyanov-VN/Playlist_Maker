package com.example.playlistmaker.search.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.playlistmaker.media.domain.interactor.ManageSearchHistoryInteractor
import com.example.playlistmaker.player.domain.interactor.FormatTimeInteractor
import com.example.playlistmaker.search.domain.entity.Track
import com.example.playlistmaker.search.domain.interactor.SearchTracksInteractor
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class SearchViewModel(
    private val searchTracksInteractor: SearchTracksInteractor,
    private val manageSearchHistoryInteractor: ManageSearchHistoryInteractor,
    private val formatTimeInteractor: FormatTimeInteractor
) : ViewModel() {

    private val _state = kotlinx.coroutines.flow.MutableStateFlow<SearchUiState>(SearchUiState.Empty)
    val state: kotlinx.coroutines.flow.StateFlow<SearchUiState> = _state.asStateFlow()

    private var lastTerm: String? = null
    private var searchJob: Job? = null

    fun search(term: String) {
        if (term.isBlank()) return

        lastTerm = term

        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            _state.value = SearchUiState.Loading

            searchTracksInteractor.execute(term)
                .catch {
                    _state.value = SearchUiState.Error
                }
                .collect { tracks ->
                    _state.value = when {
                        tracks.isNotEmpty() -> SearchUiState.Success(tracks)
                        else -> SearchUiState.NoResults
                    }
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

    fun formatTime(millis: Long?): String = formatTimeInteractor.executeForTrack(millis)

    fun getLastSearchTerm(): String? = lastTerm

    fun clearState() {
        lastTerm = null
        searchJob?.cancel()
        _state.value = SearchUiState.Empty
    }

    override fun onCleared() {
        super.onCleared()
        searchJob?.cancel()
    }
}
