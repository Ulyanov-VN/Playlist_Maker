package com.example.playlistmaker.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.playlistmaker.Track
import com.example.playlistmaker.data.repository.SearchRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SearchViewModel(private val repo: SearchRepository) : ViewModel() {
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
                val resp = repo.searchSongs(term)
                lastSearchResults = resp.results
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
     * Возвращает последний поисковый запрос
     */
    fun getLastSearchTerm(): String? = lastTerm

    /**
     * Возвращает последние результаты поиска
     */
    fun getLastSearchResults(): List<Track> = lastSearchResults

    /**
     * Восстанавливает последний поисковый запрос без выполнения нового запроса
     */
    fun restoreLastSearch() {
        lastTerm?.let { term ->
            _state.value = when {
                lastSearchResults.isNotEmpty() -> SearchUiState.Success(lastSearchResults)
                else -> SearchUiState.Empty
            }
        }
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