package com.example.playlistmaker.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.playlistmaker.data.repository.SearchRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SearchViewModel(private val repo: SearchRepository) : ViewModel() {
    private val _state = MutableStateFlow<SearchUiState>(SearchUiState.Empty)
    val state: StateFlow<SearchUiState> = _state.asStateFlow()

    private var lastTerm: String? = null

    fun search(term: String) {
        if (term.isBlank()) return
        lastTerm = term
        viewModelScope.launch {
            _state.value = SearchUiState.Loading
            try {
                val resp = repo.searchSongs(term)
                _state.value = when {
                    resp.resultCount > 0 -> SearchUiState.Success(resp.results)
                    else                 -> SearchUiState.NoResults
                }
            } catch (e: Exception) {
                _state.value = SearchUiState.Error
            }
        }
    }

    fun retry() {
        lastTerm?.let { search(it) }
    }

    /**
     * Сбрасывает текущее состояние в Empty и очищает последний терм
     */
    fun clearState() {
        lastTerm = null
        _state.value = SearchUiState.Empty
    }

    /**
     * Только сбрасывает последний терм,
     * не меняя при этом UI-состояние (если это когда-нибудь понадобится)
     */
    fun clearLastTerm() {
        lastTerm = null
    }
}
